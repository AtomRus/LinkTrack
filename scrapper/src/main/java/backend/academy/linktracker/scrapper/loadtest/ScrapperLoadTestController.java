package backend.academy.linktracker.scrapper.loadtest;

import backend.academy.linktracker.notification.LinkUpdateEventAvro;
import backend.academy.linktracker.scrapper.config.KafkaProducerConfig;
import backend.academy.linktracker.scrapper.kafka.KafkaEmitTimestampHeader;
import backend.academy.linktracker.scrapper.properties.NotificationsProperties;
import jakarta.validation.Valid;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/load-test")
@ConditionalOnProperty(prefix = "app.load-test", name = "api-enabled", havingValue = "true")
public class ScrapperLoadTestController {

    private final KafkaTemplate<Long, Object> kafkaTemplate;
    private final NotificationsProperties notificationsProperties;

    public ScrapperLoadTestController(
            @Qualifier(KafkaProducerConfig.GENERIC_KAFKA_TEMPLATE_BEAN) KafkaTemplate<Long, Object> kafkaTemplate,
            NotificationsProperties notificationsProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationsProperties = notificationsProperties;
    }

    @PostMapping("/emit-link-update")
    public ResponseEntity<Void> emitLinkUpdate(@Valid @RequestBody EmitKafkaUpdateRequest request) {
        long emittedAt = System.currentTimeMillis();
        LinkUpdateEventAvro payload = LinkUpdateEventAvro.newBuilder()
                .setId(request.linkId())
                .setUrl(request.url())
                .setDescription(request.description())
                .setTgChatIds(request.tgChatIds())
                .build();
        ProducerRecord<Long, Object> record =
                new ProducerRecord<>(notificationsProperties.getKafka().getTopic(), request.linkId(), payload);
        record.headers().add(KafkaEmitTimestampHeader.NAME, KafkaEmitTimestampHeader.encodeMillis(emittedAt));
        kafkaTemplate.send(record);
        return ResponseEntity.accepted().build();
    }
}
