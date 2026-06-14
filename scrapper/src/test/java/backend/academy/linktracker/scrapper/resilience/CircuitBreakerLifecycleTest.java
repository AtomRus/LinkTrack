package backend.academy.linktracker.scrapper.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class CircuitBreakerLifecycleTest {
    private final ResilienceExecutor resilienceExecutor = new ResilienceExecutor();

    @Test
    void shouldOpenCircuitAfterFailureThreshold() {
        CircuitBreaker circuitBreaker = testCircuitBreaker();
        Retry retry = singleAttemptRetry();

        assertThatThrownBy(() -> resilienceExecutor.execute(
                        () -> {
                            throw new RetryableHttpException("down", new RuntimeException("x"));
                        },
                        retry,
                        circuitBreaker))
                .isInstanceOf(RetryableHttpException.class);

        assertThatThrownBy(() -> resilienceExecutor.execute(
                        () -> {
                            throw new RetryableHttpException("down", new RuntimeException("x"));
                        },
                        retry,
                        circuitBreaker))
                .isInstanceOf(RetryableHttpException.class);

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void shouldRejectCallsWhenOpen() {
        CircuitBreaker circuitBreaker = testCircuitBreaker();
        Retry retry = singleAttemptRetry();

        for (int i = 0; i < 2; i++) {
            assertThatThrownBy(() -> resilienceExecutor.execute(
                            () -> {
                                throw new RetryableHttpException("down", new RuntimeException("x"));
                            },
                            retry,
                            circuitBreaker))
                    .isInstanceOf(RuntimeException.class);
        }

        assertThatThrownBy(() -> resilienceExecutor.execute(() -> "never called", retry, circuitBreaker))
                .isInstanceOf(CallNotPermittedException.class);
    }

    @Test
    void shouldMoveToHalfOpenAndCloseOnSuccessfulProbe() throws InterruptedException {
        CircuitBreaker circuitBreaker = testCircuitBreaker();
        Retry retry = singleAttemptRetry();

        for (int i = 0; i < 2; i++) {
            assertThatThrownBy(() -> resilienceExecutor.execute(
                            () -> {
                                throw new RetryableHttpException("down", new RuntimeException("x"));
                            },
                            retry,
                            circuitBreaker))
                    .isInstanceOf(RuntimeException.class);
        }

        Thread.sleep(220);
        String result = resilienceExecutor.execute(() -> "ok", retry, circuitBreaker);

        assertThat(result).isEqualTo("ok");
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldMoveToHalfOpenAndReopenOnFailedProbe() throws InterruptedException {
        CircuitBreaker circuitBreaker = testCircuitBreaker();
        Retry retry = singleAttemptRetry();

        for (int i = 0; i < 2; i++) {
            assertThatThrownBy(() -> resilienceExecutor.execute(
                            () -> {
                                throw new RetryableHttpException("down", new RuntimeException("x"));
                            },
                            retry,
                            circuitBreaker))
                    .isInstanceOf(RuntimeException.class);
        }

        Thread.sleep(220);
        assertThatThrownBy(() -> resilienceExecutor.execute(
                        () -> {
                            throw new RetryableHttpException("still down", new RuntimeException("x"));
                        },
                        retry,
                        circuitBreaker))
                .isInstanceOf(RuntimeException.class);

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    private Retry singleAttemptRetry() {
        return Retry.of("single", RetryConfig.custom().maxAttempts(1).build());
    }

    private CircuitBreaker testCircuitBreaker() {
        return CircuitBreaker.of(
                "cb",
                CircuitBreakerConfig.custom()
                        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                        .slidingWindowSize(2)
                        .minimumNumberOfCalls(2)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofMillis(200))
                        .permittedNumberOfCallsInHalfOpenState(1)
                        .recordException(ex -> ex instanceof RetryableHttpException)
                        .build());
    }
}
