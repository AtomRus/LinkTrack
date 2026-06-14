package backend.academy.linktracker.bot.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BotMetricsTest {

    private MeterRegistry meterRegistry;
    private BotMetrics botMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        botMetrics = new BotMetrics(meterRegistry);
    }

    @Test
    void shouldIncrementUserMessagesWithRequestType() {
        botMetrics.incrementUserMessage("command");
        botMetrics.incrementUserMessage("text");

        assertThat(meterRegistry.find("user_messages").tag("request_type", "command").counter().count())
                .isEqualTo(1.0);
        assertThat(meterRegistry.find("user_messages").tag("request_type", "text").counter().count())
                .isEqualTo(1.0);
    }

    @Test
    void shouldIncrementCommandsAndNotifications() {
        botMetrics.incrementCommand("/list");
        botMetrics.incrementSentNotification();

        assertThat(meterRegistry.find("command_requests").tag("command", "/list").counter().count())
                .isEqualTo(1.0);
        assertThat(meterRegistry.find("sent_notification").counter().count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordCommandDuration() {
        String result = botMetrics.record(BotMetrics.SCOPE_TELEGRAM_COMMAND, "/start", () -> "done");
        assertThat(result).isEqualTo("done");
        assertThat(meterRegistry.find("command_duration_ms")
                        .tag("scope", BotMetrics.SCOPE_TELEGRAM_COMMAND)
                        .timer()
                        .count())
                .isEqualTo(1);
    }
}
