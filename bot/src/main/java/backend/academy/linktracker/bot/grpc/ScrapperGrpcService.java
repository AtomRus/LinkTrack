package backend.academy.linktracker.bot.grpc;

import backend.academy.linktracker.bot.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.bot.exception.ResourceNotFoundException;
import backend.academy.linktracker.bot.exception.TagNotFoundException;
import backend.academy.linktracker.bot.mapper.LinkMapper;
import backend.academy.linktracker.bot.metrics.BotMetrics;
import backend.academy.linktracker.bot.model.Link;
import backend.academy.linktracker.scrapper.grpc.AddLinkRequest;
import backend.academy.linktracker.scrapper.grpc.AddTagRequest;
import backend.academy.linktracker.scrapper.grpc.GetLinksRequest;
import backend.academy.linktracker.scrapper.grpc.GetLinksRequestWithTag;
import backend.academy.linktracker.scrapper.grpc.LinkServiceGrpc;
import backend.academy.linktracker.scrapper.grpc.ListLinkResponse;
import backend.academy.linktracker.scrapper.grpc.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.grpc.RemoveTagRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@GrpcService
public class ScrapperGrpcService {

    private final LinkServiceGrpc.LinkServiceBlockingStub linkServiceBlockingStub;
    private final LinkMapper linkMapper;
    private final BotMetrics botMetrics;

    public List<Link> getLinks(Long chatId) {
        return botMetrics.record(BotMetrics.SCOPE_SCRAPPER_SYNC_API, "getLinks", () -> {
            GetLinksRequest getLinksRequest =
                    GetLinksRequest.newBuilder().setChatId(chatId).build();
            ListLinkResponse links;
            try {
                links = linkServiceBlockingStub.getLinks(getLinksRequest);
            } catch (Exception e) {
                throw new ResourceNotFoundException(
                        "Произошла непредвиденная ошибка при получения списка ссылок по chatId");
            }
            return linkMapper.mapListLinkResponseToListLink(links);
        });
    }

    public void addLink(Long chatId, Link link) {
        botMetrics.record(BotMetrics.SCOPE_SCRAPPER_SYNC_API, "addLink", () -> {
            AddLinkRequest addLinkRequest = linkMapper.toLinkRequest(link, chatId);
            try {
                linkServiceBlockingStub.addLink(addLinkRequest);
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == Status.Code.ALREADY_EXISTS) {
                    throw new LinkAlreadyTrackedException("Произошла ошибка: эта ссылка уже отслеживается");
                }
                if (e.getStatus().getCode() == Status.Code.INTERNAL) {
                    throw new LinkAlreadyTrackedException(
                            "Произошла ошибка на стороне сервера. Повторите запрос позже");
                }
                throw e;
            } catch (Exception e) {
                throw new ResourceNotFoundException("Произошла непредвиденная ошибка при добавлении ссылки");
            }
            return null;
        });
    }

    public List<Link> getLinksByTags(Long chatId, String tag) {
        return botMetrics.record(BotMetrics.SCOPE_SCRAPPER_SYNC_API, "getLinksByTag", () -> {
            GetLinksRequestWithTag getLinksRequest = GetLinksRequestWithTag.newBuilder()
                    .setChatId(chatId)
                    .setTag(tag)
                    .build();
            ListLinkResponse links;
            try {
                links = linkServiceBlockingStub.getLinksByTag(getLinksRequest);
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                    throw new TagNotFoundException("Произошла ошибка: по этому тегу ссылок нет");
                }
                throw new ResourceNotFoundException("Произошла непредвиденная ошибка при получении списка по тегу");
            } catch (Exception e) {
                throw new ResourceNotFoundException("Произошла непредвиденная ошибка при получении списка по тегу");
            }
            if (links == null) {
                throw new ResourceNotFoundException("Произошла непредвиденная ошибка при получении списка по тегу");
            }
            return linkMapper.mapListLinkResponseToListLink(links);
        });
    }

    public void removeLink(Long chatId, Link link) {
        botMetrics.record(BotMetrics.SCOPE_SCRAPPER_SYNC_API, "removeLink", () -> {
            RemoveLinkRequest request = RemoveLinkRequest.newBuilder()
                    .setChatId(chatId)
                    .setLink(link.getLink().toString())
                    .build();
            try {
                linkServiceBlockingStub.removeLink(request);
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                    throw new ResourceNotFoundException("Произошла ошибка: эта ссылка не отслеживается");
                }
                throw e;
            } catch (Exception e) {
                throw new ResourceNotFoundException("Произошла непредвиденная ошибка при удалении ссылки");
            }
            return null;
        });
    }

    public void removeTag(Long chatId, String url, String tag) {
        botMetrics.record(BotMetrics.SCOPE_SCRAPPER_SYNC_API, "removeTag", () -> {
            RemoveTagRequest request = RemoveTagRequest.newBuilder()
                    .setChatId(chatId)
                    .setLink(url)
                    .setTag(tag)
                    .build();
            try {
                linkServiceBlockingStub.removeTag(request);
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                    throw new ResourceNotFoundException("Произошла ошибка: эта ссылка не отслеживается");
                }
                throw e;
            } catch (Exception e) {
                throw new ResourceNotFoundException("Произошла непредвиденная ошибка при удалении тега");
            }
            return null;
        });
    }

    public void addTag(Long chatId, String url, String tag) {
        botMetrics.record(BotMetrics.SCOPE_SCRAPPER_SYNC_API, "addTag", () -> {
            AddTagRequest request = AddTagRequest.newBuilder()
                    .setChatId(chatId)
                    .setLink(url)
                    .setTag(tag)
                    .build();
            try {
                linkServiceBlockingStub.addTag(request);
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                    throw new ResourceNotFoundException("Произошла ошибка: эта ссылка не отслеживается");
                }
                throw e;
            } catch (Exception e) {
                throw new ResourceNotFoundException("Произошла непредвиденная ошибка при добавлении тега");
            }
            return null;
        });
    }
}
