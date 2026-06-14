package backend.academy.linktracker.bot.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.bot.exception.ResourceNotFoundException;
import backend.academy.linktracker.bot.exception.TagNotFoundException;
import backend.academy.linktracker.bot.mapper.LinkMapper;
import backend.academy.linktracker.bot.metrics.BotMetrics;
import backend.academy.linktracker.bot.model.Link;
import backend.academy.linktracker.scrapper.grpc.AddLinkRequest;
import backend.academy.linktracker.scrapper.grpc.GetLinksRequest;
import backend.academy.linktracker.scrapper.grpc.GetLinksRequestWithTag;
import backend.academy.linktracker.scrapper.grpc.LinkServiceGrpc;
import backend.academy.linktracker.scrapper.grpc.ListLinkResponse;
import backend.academy.linktracker.scrapper.grpc.RemoveLinkRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScrapperGrpcServiceTest {

    @Mock
    private LinkServiceGrpc.LinkServiceBlockingStub linkServiceBlockingStub;

    @Mock
    private LinkMapper linkMapper;

    private ScrapperGrpcService scrapperGrpcService;

    @BeforeEach
    void setUp() {
        BotMetrics botMetrics = new BotMetrics(new SimpleMeterRegistry());
        scrapperGrpcService = new ScrapperGrpcService(linkServiceBlockingStub, linkMapper, botMetrics);
    }

    @Test
    void getLinksShouldMapResponse() {
        ListLinkResponse grpcResponse = ListLinkResponse.newBuilder().setSize(0).build();
        when(linkServiceBlockingStub.getLinks(any(GetLinksRequest.class))).thenReturn(grpcResponse);
        when(linkMapper.mapListLinkResponseToListLink(grpcResponse)).thenReturn(List.of());

        assertThat(scrapperGrpcService.getLinks(1L)).isEmpty();
    }

    @Test
    void addLinkShouldTranslateAlreadyExists() {
        Link link = new Link(null, URI.create("https://github.com/a/b"), List.of());
        when(linkMapper.toLinkRequest(link, 1L)).thenReturn(AddLinkRequest.getDefaultInstance());
        when(linkServiceBlockingStub.addLink(any(AddLinkRequest.class)))
                .thenThrow(new StatusRuntimeException(Status.ALREADY_EXISTS));

        assertThatThrownBy(() -> scrapperGrpcService.addLink(1L, link))
                .isInstanceOf(LinkAlreadyTrackedException.class);
    }

    @Test
    void getLinksByTagsShouldTranslateNotFound() {
        when(linkServiceBlockingStub.getLinksByTag(any(GetLinksRequestWithTag.class)))
                .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        assertThatThrownBy(() -> scrapperGrpcService.getLinksByTags(2L, "missing"))
                .isInstanceOf(TagNotFoundException.class);
    }

    @Test
    void removeLinkShouldTranslateNotFound() {
        Link link = new Link(null, URI.create("https://github.com/a/b"), List.of());
        when(linkServiceBlockingStub.removeLink(any(RemoveLinkRequest.class)))
                .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        assertThatThrownBy(() -> scrapperGrpcService.removeLink(1L, link))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addTagShouldCallStub() {
        when(linkServiceBlockingStub.addTag(any())).thenReturn(com.google.protobuf.Empty.getDefaultInstance());

        scrapperGrpcService.addTag(5L, "https://github.com/a/b", "java");

        verify(linkServiceBlockingStub).addTag(any());
    }

    @Test
    void removeTagShouldCallStub() {
        when(linkServiceBlockingStub.removeTag(any())).thenReturn(com.google.protobuf.Empty.getDefaultInstance());

        scrapperGrpcService.removeTag(6L, "https://github.com/a/b", "java");

        verify(linkServiceBlockingStub).removeTag(any());
    }
}
