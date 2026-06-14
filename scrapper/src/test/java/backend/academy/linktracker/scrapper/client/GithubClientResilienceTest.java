package backend.academy.linktracker.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.linktracker.scrapper.metrics.OperationMetrics;
import backend.academy.linktracker.scrapper.resilience.ResilienceExecutor;
import backend.academy.linktracker.scrapper.resilience.RetryableHttpException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

class GithubClientResilienceTest {
    private static final String EVENTS_PATH = "/repos/owner/repo/events";

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
    void shouldFailWithTimeoutWhenExternalServiceIsSlow() {
        wireMockServer.stubFor(get(urlEqualTo(EVENTS_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(300)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        GithubClient client = client(100, 1, 10, Set.of(500));

        long startedAt = System.currentTimeMillis();
        assertThatThrownBy(() -> client.fetchEvents("owner", "repo", null)).isInstanceOf(ResourceAccessException.class);
        long elapsed = System.currentTimeMillis() - startedAt;

        assertThat(elapsed).isLessThan(300);
        assertThat(wireMockServer.getAllServeEvents()).hasSize(1);
    }

    @Test
    void shouldRetryOnRetryableStatusAndSucceed() {
        wireMockServer.stubFor(get(urlEqualTo(EVENTS_PATH))
                .inScenario("retry-500-500-200")
                .whenScenarioStateIs(com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("SECOND"));
        wireMockServer.stubFor(get(urlEqualTo(EVENTS_PATH))
                .inScenario("retry-500-500-200")
                .whenScenarioStateIs("SECOND")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("THIRD"));
        wireMockServer.stubFor(get(urlEqualTo(EVENTS_PATH))
                .inScenario("retry-500-500-200")
                .whenScenarioStateIs("THIRD")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        GithubClient client = client(1000, 3, 20, Set.of(500));

        var response = client.fetchEvents("owner", "repo", null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(wireMockServer.getAllServeEvents()).hasSize(3);
    }

    @Test
    void shouldNotRetryOnNonRetryableStatus() {
        wireMockServer.stubFor(
                get(urlEqualTo(EVENTS_PATH)).willReturn(aResponse().withStatus(400)));

        GithubClient client = client(1000, 3, 20, Set.of(500, 503));

        assertThatThrownBy(() -> client.fetchEvents("owner", "repo", null))
                .isInstanceOf(RestClientResponseException.class)
                .hasMessageContaining("400");
        assertThat(wireMockServer.getAllServeEvents()).hasSize(1);
    }

    @Test
    void shouldRespectConstantBackoffBetweenRetries() {
        wireMockServer.stubFor(
                get(urlEqualTo(EVENTS_PATH)).willReturn(aResponse().withStatus(500)));

        int backoffMs = 120;
        int attempts = 3;
        GithubClient client = client(1000, attempts, backoffMs, Set.of(500));

        long startedAt = System.currentTimeMillis();
        assertThatThrownBy(() -> client.fetchEvents("owner", "repo", null)).isInstanceOf(RetryableHttpException.class);
        long elapsed = System.currentTimeMillis() - startedAt;

        assertThat(wireMockServer.getAllServeEvents()).hasSize(attempts);
        assertThat(elapsed).isGreaterThanOrEqualTo((long) backoffMs * (attempts - 1));
    }

    private GithubClient client(int timeoutMs, int maxAttempts, int backoffMs, Set<Integer> retryableCodes) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);

        RestClient restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(wireMockServer.baseUrl())
                .build();

        Retry retry = Retry.of(
                "testRetry",
                RetryConfig.custom()
                        .maxAttempts(maxAttempts)
                        .waitDuration(Duration.ofMillis(backoffMs))
                        .retryOnException(
                                ex -> ex instanceof RetryableHttpException || ex instanceof ResourceAccessException)
                        .build());

        CircuitBreaker circuitBreaker = CircuitBreaker.of(
                "testCb",
                CircuitBreakerConfig.custom()
                        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                        .slidingWindowSize(10)
                        .minimumNumberOfCalls(10)
                        .failureRateThreshold(100)
                        .waitDurationInOpenState(Duration.ofSeconds(1))
                        .permittedNumberOfCallsInHalfOpenState(5)
                        .recordException(
                                ex -> ex instanceof RetryableHttpException || ex instanceof ResourceAccessException)
                        .build());

        return new GithubClient(
                restClient,
                retry,
                circuitBreaker,
                retryableCodes,
                new ResilienceExecutor(),
                new OperationMetrics(new SimpleMeterRegistry()));
    }
}
