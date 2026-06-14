package backend.academy.linktracker.bot.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class BotMetrics {

    public static final String SCOPE_SCRAPPER_SYNC_API = "scrapper_sync_api";
    public static final String SCOPE_SCRAPPER_ASYNC_API = "scrapper_async_api";
    public static final String SCOPE_TELEGRAM_COMMAND = "telegram_command";

    private final MeterRegistry meterRegistry;

    public BotMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementUserMessage(String requestType) {
        Counter.builder("user_messages")
                .description("Счётчик входящих сообщений пользователя в Telegram")
                .tag("request_type", requestType)
                .register(meterRegistry)
                .increment();
    }

    public void incrementCommand(String command) {
        Counter.builder("command_requests")
                .description("Счётчик обработанных команд бота")
                .tag("command", command)
                .register(meterRegistry)
                .increment();
    }

    public void incrementSentNotification() {
        Counter.builder("sent_notification")
                .description("Счётчик отправленных уведомлений пользователям")
                .register(meterRegistry)
                .increment();
    }

    public void recordDuration(String scope, String scopeType, long durationMs) {
        Timer.builder("command_duration_ms")
                .description("Длительность обработки команды или вызова API в миллисекундах")
                .tag("scope", scope)
                .tag("scope_type", scopeType)
                .publishPercentileHistogram()
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public <T> T record(String scope, String scopeType, java.util.function.Supplier<T> action) {
        long start = System.nanoTime();
        try {
            return action.get();
        } finally {
            recordDuration(scope, scopeType, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
        }
    }
}
