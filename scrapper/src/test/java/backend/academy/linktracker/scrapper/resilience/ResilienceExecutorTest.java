package backend.academy.linktracker.scrapper.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

class ResilienceExecutorTest {
    private final ResilienceExecutor resilienceExecutor = new ResilienceExecutor();

    @Test
    void shouldRetryRetryableException() {
        Retry retry = Retry.of(
                "retry",
                RetryConfig.custom()
                        .maxAttempts(3)
                        .waitDuration(Duration.ofMillis(1))
                        .retryOnException(ex -> ex instanceof RetryableHttpException)
                        .build());
        CircuitBreaker circuitBreaker = closedCircuitBreaker();
        AtomicInteger attempts = new AtomicInteger();

        String result = resilienceExecutor.execute(
                () -> {
                    if (attempts.incrementAndGet() < 3) {
                        throw new RetryableHttpException("retry", new ResourceAccessException("io"));
                    }
                    return "ok";
                },
                retry,
                circuitBreaker);

        assertThat(result).isEqualTo("ok");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void shouldNotRetryNonRetryableException() {
        Retry retry = Retry.of(
                "retry",
                RetryConfig.custom()
                        .maxAttempts(3)
                        .waitDuration(Duration.ofMillis(1))
                        .retryOnException(ex -> ex instanceof RetryableHttpException)
                        .build());
        CircuitBreaker circuitBreaker = closedCircuitBreaker();
        AtomicInteger attempts = new AtomicInteger();

        assertThatThrownBy(() -> resilienceExecutor.execute(
                        () -> {
                            attempts.incrementAndGet();
                            throw new IllegalArgumentException("bad request");
                        },
                        retry,
                        circuitBreaker))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(attempts.get()).isEqualTo(1);
    }

    private CircuitBreaker closedCircuitBreaker() {
        return CircuitBreaker.of(
                "cb",
                CircuitBreakerConfig.custom()
                        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                        .slidingWindowSize(10)
                        .minimumNumberOfCalls(10)
                        .failureRateThreshold(100)
                        .waitDurationInOpenState(Duration.ofMillis(50))
                        .permittedNumberOfCallsInHalfOpenState(1)
                        .recordException(ex -> true)
                        .build());
    }
}
