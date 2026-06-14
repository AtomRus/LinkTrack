package backend.academy.linktracker.loadservice.loadtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.grpc.GetLinksRequest;
import backend.academy.linktracker.scrapper.grpc.LinkServiceGrpc;
import backend.academy.linktracker.scrapper.grpc.ListLinkResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Empty;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoadTestServiceTest {

    @Mock
    private LinkServiceGrpc.LinkServiceBlockingStub linkServiceBlockingStub;

    private LoadTestService loadTestService;

    @BeforeEach
    void setUp() {
        loadTestService = new LoadTestService(linkServiceBlockingStub, new ObjectMapper());
    }

    @Test
    void shouldRunListReadsWithWarmup() {
        when(linkServiceBlockingStub.getLinks(any(GetLinksRequest.class)))
                .thenReturn(ListLinkResponse.newBuilder().setSize(0).build());

        LoadSummaryResponse response = loadTestService.runListReads(new ListReadLoadRequest(1001L, 10, 2, 3));

        assertThat(response.successCount()).isEqualTo(10);
        assertThat(response.errorCount()).isZero();
        verify(linkServiceBlockingStub, times(13)).getLinks(any(GetLinksRequest.class));
    }

    @Test
    void shouldCountSeedFailures() {
        AtomicInteger calls = new AtomicInteger();
        when(linkServiceBlockingStub.addLink(any())).thenAnswer(inv -> {
            if (calls.incrementAndGet() % 2 == 0) {
                throw new RuntimeException("fail");
            }
            return Empty.getDefaultInstance();
        });

        LoadSummaryResponse response =
                loadTestService.seedScrapper(new SeedScrapperRequest(4, 1001L, "https://x/", 1));

        assertThat(response.successCount()).isEqualTo(2);
        assertThat(response.errorCount()).isEqualTo(2);
    }
}
