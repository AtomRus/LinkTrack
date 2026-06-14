package backend.academy.linktracker.bot.kafka;

import backend.academy.linktracker.bot.dto.LinkUpdateEvent;
import backend.academy.linktracker.bot.dto.ProcessedLinkUpdateEvent;
import backend.academy.linktracker.bot.metrics.BotMetrics;
import backend.academy.linktracker.bot.service.LinkUpdateHandler;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkUpdatesKafkaListener {

    static final String EMITTED_AT_HEADER = "lt-emitted-at";

    private final LinkUpdateHandler linkUpdateHandler;
    private final Validator validator;
    private final MeterRegistry meterRegistry;
    private final BotMetrics botMetrics;

    @KafkaListener(
            topics = "${app.notifications.kafka.topic:link.processed-updates}",
            groupId = "${app.notifications.kafka.group-id:bot-link-updates}")
    public void onMessage(
            ProcessedLinkUpdateEvent event, @Header(name = EMITTED_AT_HEADER, required = false) byte[] emittedAtRaw) {
        LinkUpdateEvent dtoEvent = new LinkUpdateEvent(event.id(), null, event.description(), event.tgChatIds());
        Set<ConstraintViolation<LinkUpdateEvent>> violations = validator.validate(dtoEvent);
        if (!violations.isEmpty()) {
            throw new jakarta.validation.ConstraintViolationException(violations);
        }

        botMetrics.record(BotMetrics.SCOPE_SCRAPPER_ASYNC_API, "processLinkUpdate", () -> {
            linkUpdateHandler.handleEvent(dtoEvent);
            return null;
        });

        long emittedAt = decodeMillis(emittedAtRaw);
        if (emittedAt > 0) {
            long lagMs = System.currentTimeMillis() - emittedAt;
            Timer.builder("linktracker.kafka.e2e.latency")
                    .description("Kafka: от метки времени в заголовке до завершения обработки в боте")
                    .publishPercentileHistogram()
                    .register(meterRegistry)
                    .record(Math.max(0, lagMs), TimeUnit.MILLISECONDS);
        }
    }

    private static long decodeMillis(byte[] raw) {
        if (raw == null || raw.length < Long.BYTES) {
            return -1L;
        }
        return ByteBuffer.wrap(raw).getLong();
    }
}
