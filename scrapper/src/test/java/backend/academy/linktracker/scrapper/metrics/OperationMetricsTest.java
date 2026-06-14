package backend.academy.linktracker.scrapper.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OperationMetricsTest {

    private MeterRegistry meterRegistry;
    private OperationMetrics metrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metrics = new OperationMetrics(meterRegistry);
    }

    @Test
    void shouldRecordDurationAndApiRequests() {
        metrics.incrementApiRequests("grpc");
        metrics.recordDuration(OperationMetrics.SCOPE_DATABASE, "LinkRepository.findAll", 42L);

        Counter apiCounter = meterRegistry.find("api_requests").tag("source", "grpc").counter();
        assertThat(apiCounter).isNotNull();
        assertThat(apiCounter.count()).isEqualTo(1.0);
        assertThat(meterRegistry.find("request_duration_ms")
                        .tag("scope", OperationMetrics.SCOPE_DATABASE)
                        .timer()
                        .count())
                .isEqualTo(1);
    }

    @Test
    void shouldRecordSupplierAndRunnable() {
        String result = metrics.record(OperationMetrics.SCOPE_KAFKA, "topic", () -> "ok");
        assertThat(result).isEqualTo("ok");

        metrics.record(OperationMetrics.SCOPE_EXTERNAL_SOURCE, "github.com", () -> {});

        assertThat(meterRegistry.find("request_duration_ms")
                        .tag("scope", OperationMetrics.SCOPE_EXTERNAL_SOURCE)
                        .timers())
                .hasSize(1);
    }

    @Test
    void shouldRecordScrapeDuration() {
        metrics.recordScrapeDuration("github", 15L);
        assertThat(meterRegistry.find("request_duration_ms")
                        .tag("scope_type", "github")
                        .timer()
                        .count())
                .isEqualTo(1);
    }
}
