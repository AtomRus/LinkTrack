package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.properties.ResilienceProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {

    @Bean("githubRetry")
    public Retry githubRetry(ResilienceProperties properties) {
        return buildRetry("githubRetry", properties.getGithub().getRetry());
    }

    @Bean("stackoverflowRetry")
    public Retry stackoverflowRetry(ResilienceProperties properties) {
        return buildRetry("stackoverflowRetry", properties.getStackoverflow().getRetry());
    }

    @Bean("notificationHttpRetry")
    public Retry notificationHttpRetry(ResilienceProperties properties) {
        return buildRetry(
                "notificationHttpRetry", properties.getNotificationHttp().getRetry());
    }

    @Bean("githubCircuitBreaker")
    public CircuitBreaker githubCircuitBreaker(ResilienceProperties properties) {
        return buildCircuitBreaker(
                "githubCircuitBreaker", properties.getGithub().getCircuitBreaker());
    }

    @Bean("stackoverflowCircuitBreaker")
    public CircuitBreaker stackoverflowCircuitBreaker(ResilienceProperties properties) {
        return buildCircuitBreaker(
                "stackoverflowCircuitBreaker", properties.getStackoverflow().getCircuitBreaker());
    }

    @Bean("notificationHttpCircuitBreaker")
    public CircuitBreaker notificationHttpCircuitBreaker(ResilienceProperties properties) {
        return buildCircuitBreaker(
                "notificationHttpCircuitBreaker",
                properties.getNotificationHttp().getCircuitBreaker());
    }

    private Retry buildRetry(String name, ResilienceProperties.Retry retryProperties) {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(retryProperties.getMaxAttempts())
                .waitDuration(Duration.ofMillis(retryProperties.getBackoffMs()))
                .retryOnException(RetryableExceptionPredicate.INSTANCE)
                .build();
        return Retry.of(name, config);
    }

    private CircuitBreaker buildCircuitBreaker(String name, ResilienceProperties.CircuitBreaker properties) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(properties.getFailureRateThreshold())
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(properties.getSlidingWindowSize())
                .minimumNumberOfCalls(properties.getMinimumNumberOfCalls())
                .waitDurationInOpenState(Duration.ofMillis(properties.getWaitDurationInOpenStateMs()))
                .permittedNumberOfCallsInHalfOpenState(properties.getPermittedCallsInHalfOpenState())
                .recordException(RetryableExceptionPredicate.INSTANCE)
                .build();
        return CircuitBreaker.of(name, config);
    }

    private enum RetryableExceptionPredicate implements java.util.function.Predicate<Throwable> {
        INSTANCE;

        @Override
        public boolean test(Throwable throwable) {
            return throwable instanceof backend.academy.linktracker.scrapper.resilience.RetryableHttpException
                    || throwable instanceof org.springframework.web.client.ResourceAccessException;
        }
    }
}
