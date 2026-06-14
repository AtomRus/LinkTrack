package backend.academy.linktracker.scrapper.grpc;

import backend.academy.linktracker.scrapper.cache.ClientSideLinksCache;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.exception.ResourceNotFoundException;
import backend.academy.linktracker.scrapper.exception.TagNotFoundException;
import backend.academy.linktracker.scrapper.mapper.LinkMapper;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.service.LinkService;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@GrpcService
@Slf4j
public class LinkGrpcService extends LinkServiceGrpc.LinkServiceImplBase {

    private final LinkService linkService;
    private final LinkMapper linkMapper;
    private final MeterRegistry meterRegistry;
    private final ClientSideLinksCache clientSideLinksCache;

    @Override
    public void getLinks(GetLinksRequest request, StreamObserver<ListLinkResponse> responseObserver) {
        MDC.put("chatId", String.valueOf(request.getChatId()));
        log.debug("Получен запрос на получение списка ссылок для пользователя {}", request.getChatId());
        List<Link> links = clientSideLinksCache.get(request.getChatId()).orElseGet(() -> {
            List<Link> resolved = linkService.getListOfLinks(request.getChatId());
            clientSideLinksCache.put(request.getChatId(), resolved);
            return resolved;
        });

        List<LinkResponse> linkResponses = linkMapper.toLinkResponse(links);
        ListLinkResponse listLinkResponse = ListLinkResponse.newBuilder()
                .addAllLinks(linkResponses)
                .setSize(linkResponses.size())
                .build();
        log.debug(
                "Возвращаем пользователю {} список из ссылкок {}",
                request.getChatId(),
                listLinkResponse.getLinksList().toString());
        MDC.put("linkList", listLinkResponse.getLinksList().toString());
        responseObserver.onNext(listLinkResponse);
        MDC.clear();
        responseObserver.onCompleted();
    }

    @Override
    public void getLinksByTag(GetLinksRequestWithTag request, StreamObserver<ListLinkResponse> responseObserver) {
        MDC.put("chatId", String.valueOf(request.getChatId()));
        MDC.put("tag", request.getTag());
        log.debug(
                "Получен запрос на получение списка ссылок по тегу {} для пользователя {}",
                request.getTag(),
                request.getChatId());
        List<Link> links;
        try {
            links = linkService.getListOfLinksByTag(request.getChatId(), request.getTag());
        } catch (TagNotFoundException tagNotFoundException) {
            log.error("Получен запрос на получение списка ссылок для пользователя {}", request.getChatId());
            MDC.put("isFailed", "true");
            MDC.put("errorMessage", tagNotFoundException.getMessage());
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
            MDC.clear();
            responseObserver.onCompleted();
            return;
        }
        MDC.put("isFailed", "false");
        List<LinkResponse> linkResponses = linkMapper.toLinkResponse(links);
        ListLinkResponse listLinkResponse = ListLinkResponse.newBuilder()
                .addAllLinks(linkResponses)
                .setSize(linkResponses.size())
                .build();
        responseObserver.onNext(listLinkResponse);
        MDC.clear();
        responseObserver.onCompleted();
    }

    @Override
    public void addLink(AddLinkRequest request, StreamObserver<Empty> responseObserver) {
        MDC.put("chatId", String.valueOf(request.getChatId()));
        MDC.put("tagList", request.getTagsList().toString());
        MDC.put("link", request.getLink());
        log.debug(
                "Получен запрос на добавление ссылки {} с тегами {} для пользователя {}",
                request.getLink(),
                request.getTagsList().toString(),
                request.getChatId());
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            linkService.addLink(request.getChatId(), request.getLink(), request.getTagsList());
        } catch (LinkAlreadyTrackedException e) {
            MDC.put("errorName", e.getMessage());
            log.error("Полученна ошибка {}", e.getMessage());
        } finally {
            sample.stop(Timer.builder("grpc.server.requests")
                    .description("gRPC LinkService server handling time")
                    .tag("method", "addLink")
                    .publishPercentileHistogram()
                    .register(meterRegistry));
        }
        MDC.clear();
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void removeLink(RemoveLinkRequest request, StreamObserver<Empty> responseObserver) {
        MDC.put("chatId", String.valueOf(request.getChatId()));
        MDC.put("link", request.getLink());
        log.debug("Получен запрос на удаления ссылки {} для пользователя {}", request.getLink(), request.getChatId());
        try {
            linkService.removeLink(request.getChatId(), request.getLink());
        } catch (ResourceNotFoundException e) {
            MDC.put("errorName", e.getMessage());
            log.error("Произошла ошибка при удалении ссылки: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
            responseObserver.onCompleted();
        }
        MDC.clear();
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void addTag(AddTagRequest request, StreamObserver<Empty> responseObserver) {
        MDC.put("chatId", String.valueOf(request.getChatId()));
        MDC.put("link", request.getLink());
        MDC.put("tag", request.getTag());
        log.debug(
                "Получен запрос на добавление тега {} для ссылки {} пользователя {}",
                request.getTag(),
                request.getLink(),
                request.getChatId());
        try {
            linkService.addTagToLink(request.getChatId(), request.getLink(), request.getTag());

        } catch (ResourceNotFoundException | LinkNotFoundException e) {
            log.error("Произошла ошибка данных при добавлении тега: {}", e.getMessage());
            MDC.put("errorName", e.getMessage());
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Внутренняя ошибка сервера при добавлении тега: " + e.getMessage())
                    .asRuntimeException());
        }
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void removeTag(RemoveTagRequest request, StreamObserver<Empty> responseObserver) {
        try {
            linkService.removeTagFromLink(request.getChatId(), request.getLink(), request.getTag());

        } catch (ResourceNotFoundException | LinkNotFoundException | TagNotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());

        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Некорректный запрос: " + e.getMessage())
                    .asRuntimeException());

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Внутренняя ошибка сервера при удалении тега")
                    .withCause(e)
                    .asRuntimeException());
        }
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
