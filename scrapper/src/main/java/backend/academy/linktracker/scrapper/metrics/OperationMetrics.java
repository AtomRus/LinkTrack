package backend.academy.linktracker.scrapper.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class OperationMetrics {

    public static final String SCOPE_DATABASE = "database";
    public static final String SCOPE_EXTERNAL_SOURCE = "external_source";
    public static final String SCOPE_KAFKA = "kafka";
    public static final String SCOPE_LLM_AGENT = "llm_agent";

    private final MeterRegistry meterRegistry;

    public OperationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordDuration(String scope, String scopeType, long durationMs) {
        Timer.builder("request_duration_ms")
                .description("Длительность операции в миллисекундах")
                .tag("scope", scope)
                .tag("scope_type", scopeType)
                .publishPercentileHistogram()
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordScrapeDuration(String trackedSource, long durationMs) {
        recordDuration(SCOPE_EXTERNAL_SOURCE, trackedSource, durationMs);
    }

    public void incrementApiRequests(String source) {
        Counter.builder("api_requests")
                .description("Счётчик запросов к API Scrapper")
                .tag("source", source)
                .register(meterRegistry)
                .increment();
    }

    public <T> T record(String scope, String scopeType, java.util.function.Supplier<T> action) {
        long start = System.nanoTime();
        try {
            return action.get();
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            recordDuration(scope, scopeType, durationMs);
        }
    }

    public void record(String scope, String scopeType, Runnable action) {
        record(scope, scopeType, () -> {
            action.run();
            return null;
        });
    }
}
