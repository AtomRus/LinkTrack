package backend.academy.linktracker.scrapper.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.cache.ClientSideLinksCache;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exception.ResourceNotFoundException;
import backend.academy.linktracker.scrapper.exception.TagNotFoundException;
import backend.academy.linktracker.scrapper.mapper.LinkMapper;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.service.LinkService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkGrpcServiceTest {

    @Mock
    private LinkService linkService;

    @Mock
    private LinkMapper linkMapper;

    @Mock
    private ClientSideLinksCache clientSideLinksCache;

    private LinkGrpcService linkGrpcService;

    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        linkGrpcService = new LinkGrpcService(linkService, linkMapper, meterRegistry, clientSideLinksCache);
    }

    @Test
    void getLinksShouldUseCacheAndReturnMappedResponse() {
        Link link = new Link(1L, 10L, "https://github.com/a/b", null, null, null, List.of("java"));
        when(clientSideLinksCache.get(1L)).thenReturn(Optional.of(List.of(link)));
        when(linkMapper.toLinkResponse(anyList()))
                .thenReturn(List.of(LinkResponse.newBuilder().setLink("https://github.com/a/b").build()));

        CapturingObserver<ListLinkResponse> observer = new CapturingObserver<>();
        linkGrpcService.getLinks(GetLinksRequest.newBuilder().setChatId(1L).build(), observer);

        assertThat(observer.response.getSize()).isEqualTo(1);
        verify(linkService, org.mockito.Mockito.never()).getListOfLinks(anyLong());
    }

    @Test
    void getLinksByTagShouldReturnNotFoundOnMissingTag() {
        when(linkService.getListOfLinksByTag(2L, "missing"))
                .thenThrow(new TagNotFoundException("tag missing"));

        CapturingObserver<ListLinkResponse> observer = new CapturingObserver<>();
        linkGrpcService.getLinksByTag(
                GetLinksRequestWithTag.newBuilder().setChatId(2L).setTag("missing").build(), observer);

        assertThat(observer.error).isInstanceOf(StatusRuntimeException.class);
        assertThat(((StatusRuntimeException) observer.error).getStatus().getCode())
                .isEqualTo(Status.Code.NOT_FOUND);
    }

    @Test
    void addLinkShouldCompleteEvenWhenAlreadyTracked() {
        doThrow(new LinkAlreadyTrackedException("exists"))
                .when(linkService)
                .addLink(eq(3L), eq("https://x"), anyList());

        CapturingObserver<com.google.protobuf.Empty> observer = new CapturingObserver<>();
        linkGrpcService.addLink(
                AddLinkRequest.newBuilder().setChatId(3L).setLink("https://x").build(), observer);

        assertThat(observer.response).isNotNull();
        assertThat(observer.error).isNull();
    }

    @Test
    void removeLinkShouldPropagateNotFound() {
        doThrow(new ResourceNotFoundException("missing"))
                .when(linkService)
                .removeLink(4L, "https://missing");

        CapturingObserver<com.google.protobuf.Empty> observer = new CapturingObserver<>();
        linkGrpcService.removeLink(
                RemoveLinkRequest.newBuilder().setChatId(4L).setLink("https://missing").build(), observer);

        assertThat(((StatusRuntimeException) observer.error).getStatus().getCode())
                .isEqualTo(Status.Code.NOT_FOUND);
    }

    @Test
    void addTagShouldReturnInvalidArgumentForIllegalArgument() {
        doThrow(new IllegalArgumentException("bad tag"))
                .when(linkService)
                .addTagToLink(5L, "https://x", "!!!");

        CapturingObserver<com.google.protobuf.Empty> observer = new CapturingObserver<>();
        linkGrpcService.addTag(
                AddTagRequest.newBuilder()
                        .setChatId(5L)
                        .setLink("https://x")
                        .setTag("!!!")
                        .build(),
                observer);

        assertThat(((StatusRuntimeException) observer.error).getStatus().getCode())
                .isEqualTo(Status.Code.INVALID_ARGUMENT);
    }

    private static final class CapturingObserver<T> implements StreamObserver<T> {
        private T response;
        private Throwable error;

        @Override
        public void onNext(T value) {
            this.response = value;
        }

        @Override
        public void onError(Throwable t) {
            this.error = t;
        }

        @Override
        public void onCompleted() {}
    }
}
