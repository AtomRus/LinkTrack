package backend.academy.linktracker.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.metrics.OperationMetrics;
import backend.academy.linktracker.scrapper.resilience.ResilienceExecutor;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

class StackOverflowClientTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void fetchAnswersShouldReturnParsedItems() {
        wireMockServer.stubFor(get(urlPathEqualTo("/questions/123/answers"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                """
                                {"items":[],"has_more":false,"quota_max":100,"quota_remaining":99}
                                """)));

        StackOverflowClient client = client();

        var response = client.fetchAnswers(123L, null);

        assertThat(response.items()).isEmpty();
    }

    @Test
    void fetchCommentsShouldReturnParsedItems() {
        wireMockServer.stubFor(get(urlPathEqualTo("/questions/123/comments"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                """
                                {"items":[],"has_more":false,"quota_max":100,"quota_remaining":99}
                                """)));

        StackOverflowClient client = client();

        var response = client.fetchComments(123L, null);

        assertThat(response.items()).isEmpty();
    }

    private StackOverflowClient client() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(1000);
        requestFactory.setReadTimeout(1000);

        RestClient restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(wireMockServer.baseUrl())
                .build();

        Retry retry = Retry.of("soRetry", RetryConfig.custom().maxAttempts(1).build());
        CircuitBreaker circuitBreaker = CircuitBreaker.of(
                "soCb", CircuitBreakerConfig.custom().slidingWindowSize(10).minimumNumberOfCalls(1).build());

        return new StackOverflowClient(
                restClient,
                retry,
                circuitBreaker,
                Set.of(500),
                new ResilienceExecutor(),
                new OperationMetrics(new SimpleMeterRegistry()));
    }
}
