package backend.academy.linktracker.scrapper.outbox;

import backend.academy.linktracker.scrapper.config.RawUpdatesKafkaProducerConfig;
import backend.academy.linktracker.scrapper.dto.RawLinkUpdate;
import backend.academy.linktracker.scrapper.kafka.KafkaEmitTimestampHeader;
import backend.academy.linktracker.scrapper.metrics.OperationMetrics;
import backend.academy.linktracker.scrapper.properties.NotificationsProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.notifications", name = "transport", havingValue = "kafka", matchIfMissing = true)
public class OutboxPublisher {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Qualifier(RawUpdatesKafkaProducerConfig.RAW_UPDATES_KAFKA_TEMPLATE_BEAN)
    private final KafkaTemplate<Long, String> rawUpdatesKafkaTemplate;

    private final NotificationsProperties notificationsProperties;
    private final OperationMetrics operationMetrics;

    @Scheduled(fixedDelayString = "${app.notifications.outbox.publish-interval-ms:1000}")
    @Transactional
    public void publishBatch() {
        NotificationsProperties.Outbox outboxProps = notificationsProperties.getOutbox();
        List<OutboxEventEntity> batch = outboxRepository.findNextNew(outboxProps.getBatchSize());
        for (OutboxEventEntity e : batch) {
            try {
                List<?> rawIds = objectMapper.readValue(e.getTgChatIdsJson(), java.util.ArrayList.class);
                List<Long> chatIds =
                        rawIds.stream().map(v -> ((Number) v).longValue()).collect(Collectors.toList());

                RawLinkUpdate payload = new RawLinkUpdate(e.getLinkId(), e.getDescription(), e.getAuthor(), chatIds);
                String jsonPayload = objectMapper.writeValueAsString(payload);
                long emittedAt = System.currentTimeMillis();
                ProducerRecord<Long, String> record = new ProducerRecord<>(
                        notificationsProperties.getKafka().getRawTopic(), e.getLinkId(), jsonPayload);
                record.headers().add(KafkaEmitTimestampHeader.NAME, KafkaEmitTimestampHeader.encodeMillis(emittedAt));
                operationMetrics.record(
                        OperationMetrics.SCOPE_KAFKA,
                        notificationsProperties.getKafka().getRawTopic(),
                        () -> {
                            try {
                                rawUpdatesKafkaTemplate
                                        .send(record)
                                        .get(outboxProps.getSendTimeoutMs(), TimeUnit.MILLISECONDS);
                            } catch (Exception ex) {
                                throw new IllegalStateException(ex);
                            }
                        });
                e.setStatus(OutboxStatus.SENT);
                e.setSentAt(OffsetDateTime.now());
                e.setLastError(null);
            } catch (Exception ex) {
                e.setAttempts(e.getAttempts() + 1);
                e.setLastError(ex.getMessage());
                if (e.getAttempts() >= outboxProps.getMaxAttempts()) {
                    e.setStatus(OutboxStatus.FAILED);
                }
                log.warn("Failed to publish outbox event {}: {}", e.getEventId(), ex.getMessage());
            }
            outboxRepository.save(e);
        }
    }
}
