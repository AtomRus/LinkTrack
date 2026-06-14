package backend.academy.linktracker.loadservice.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.loadservice.client.GithubSearchClient;
import backend.academy.linktracker.loadservice.client.StackExchangeSearchClient;
import backend.academy.linktracker.scrapper.grpc.LinkServiceGrpc;
import com.google.protobuf.Empty;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoadControllerTest {

    @Mock
    private LinkServiceGrpc.LinkServiceBlockingStub linkServiceBlockingStub;

    @Mock
    private GithubSearchClient githubSearchClient;

    @Mock
    private StackExchangeSearchClient stackExchangeSearchClient;

    @InjectMocks
    private LoadController loadController;

    @Test
    void runShouldUseDefaultGithubQueryWhenBlank() {
        when(githubSearchClient.fetchRepositoryUrls(eq("language:java stars:>100"), eq(2)))
                .thenReturn(List.of("https://github.com/a/b", "https://github.com/c/d"));
        when(linkServiceBlockingStub.addLink(any())).thenReturn(Empty.getDefaultInstance());

        LoadRunResponse response = loadController.run(new LoadRunRequest(LoadRunRequest.Source.github, 2, 1001L, ""));

        assertThat(response.succeeded()).isEqualTo(2);
        assertThat(response.failed()).isZero();
        verify(githubSearchClient).fetchRepositoryUrls("language:java stars:>100", 2);
    }

    @Test
    void runShouldCountStackOverflowFailures() {
        when(stackExchangeSearchClient.fetchQuestionUrls(2))
                .thenReturn(List.of("https://stackoverflow.com/q/1", "https://stackoverflow.com/q/2"));
        when(linkServiceBlockingStub.addLink(any()))
                .thenReturn(Empty.getDefaultInstance())
                .thenThrow(new RuntimeException("fail"));

        LoadRunResponse response =
                loadController.run(new LoadRunRequest(LoadRunRequest.Source.stackoverflow, 2, 1001L, null));

        assertThat(response.succeeded()).isEqualTo(1);
        assertThat(response.failed()).isEqualTo(1);
    }
}
