package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@NoArgsConstructor
@EqualsAndHashCode
@ConfigurationProperties(prefix = "app.resilience")
public class ResilienceProperties {
    @Valid
    @NotNull
    private Client github = new Client();

    @Valid
    @NotNull
    private Client stackoverflow = new Client();

    @Valid
    @NotNull
    private Client notificationHttp = new Client();

    @Valid
    @NotNull
    private RateLimit rateLimit = new RateLimit();

    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Client {
        @Min(1)
        private int timeoutMs = 3000;

        @Valid
        @NotNull
        private Retry retry = new Retry();

        @Valid
        @NotNull
        private CircuitBreaker circuitBreaker = new CircuitBreaker();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Retry {
        @Min(1)
        private int maxAttempts = 3;

        @Min(1)
        private int backoffMs = 300;

        @NotEmpty
        private List<Integer> retryableStatusCodes = List.of(429, 500, 502, 503, 504);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class CircuitBreaker {
        @Min(1)
        private int failureRateThreshold = 50;

        @Min(1)
        private int slidingWindowSize = 10;

        @Min(1)
        private int minimumNumberOfCalls = 5;

        @Min(1)
        private int waitDurationInOpenStateMs = 30000;

        @Min(1)
        private int permittedCallsInHalfOpenState = 3;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class RateLimit {
        private boolean enabled = true;

        @Min(1)
        private long capacity = 30;

        @Min(1)
        private long refillTokens = 30;

        @Min(1)
        private long refillPeriodSeconds = 60;
    }
}
