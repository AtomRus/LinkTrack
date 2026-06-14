package backend.academy.linktracker.scrapper.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.metrics.OperationMetrics;
import backend.academy.linktracker.scrapper.properties.NotificationsProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private KafkaTemplate<Long, String> rawUpdatesKafkaTemplate;

    @Mock
    private OperationMetrics operationMetrics;

    private OutboxPublisher outboxPublisher;
    private NotificationsProperties notificationsProperties;

    @BeforeEach
    void setUp() {
        notificationsProperties = new NotificationsProperties();
        notificationsProperties.getKafka().setRawTopic("link.raw-updates");
        notificationsProperties.getOutbox().setBatchSize(10);
        notificationsProperties.getOutbox().setSendTimeoutMs(1000);
        notificationsProperties.getOutbox().setMaxAttempts(3);

        outboxPublisher = new OutboxPublisher(
                outboxRepository, new ObjectMapper(), rawUpdatesKafkaTemplate, notificationsProperties, operationMetrics);

        org.mockito.Mockito.doAnswer(inv -> {
                    inv.getArgument(2, Runnable.class).run();
                    return null;
                })
                .when(operationMetrics)
                .record(any(), any(), any(Runnable.class));
    }

    @Test
    void publishBatchShouldMarkEventAsSent() throws Exception {
        OutboxEventEntity event = new OutboxEventEntity();
        event.setEventId(1L);
        event.setLinkId(42L);
        event.setDescription("desc");
        event.setAuthor("alice");
        event.setTgChatIdsJson("[111]");
        event.setStatus(OutboxStatus.NEW);

        when(outboxRepository.findNextNew(10)).thenReturn(List.of(event));
        RecordMetadata metadata = new RecordMetadata(new TopicPartition("link.raw-updates", 0), 0, 0, 0, 0, 0);
        SendResult<Long, String> sendResult = new SendResult<>(null, metadata);
        when(rawUpdatesKafkaTemplate.send(any(org.apache.kafka.clients.producer.ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        outboxPublisher.publishBatch();

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(captor.getValue().getSentAt()).isNotNull();
    }

    @Test
    void publishBatchShouldIncrementAttemptsOnFailure() {
        OutboxEventEntity event = new OutboxEventEntity();
        event.setEventId(2L);
        event.setLinkId(99L);
        event.setDescription("desc");
        event.setAuthor("bob");
        event.setTgChatIdsJson("[222]");
        event.setStatus(OutboxStatus.NEW);
        event.setAttempts(0);

        when(outboxRepository.findNextNew(10)).thenReturn(List.of(event));
        when(rawUpdatesKafkaTemplate.send(any(org.apache.kafka.clients.producer.ProducerRecord.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")));

        outboxPublisher.publishBatch();

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxRepository).save(captor.capture());
        assertThat(captor.getValue().getAttempts()).isEqualTo(1);
        assertThat(captor.getValue().getLastError()).contains("kafka down");
    }
}
