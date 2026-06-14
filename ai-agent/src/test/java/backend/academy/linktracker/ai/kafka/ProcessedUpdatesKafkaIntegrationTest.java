package backend.academy.linktracker.ai.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import backend.academy.linktracker.ai.KafkaTestTopicsConfiguration;
import backend.academy.linktracker.ai.dto.ProcessedLinkUpdate;
import backend.academy.linktracker.ai.dto.RawLinkUpdate;
import backend.academy.linktracker.ai.model.UpdatePriority;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@Import(KafkaTestTopicsConfiguration.class)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ProcessedUpdatesKafkaIntegrationTest {

    private static final String RAW_TOPIC = "link.raw-updates";
    private static final String PROCESSED_TOPIC = "link.processed-updates";

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("apache/kafka-native:4.1.1"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldPublishProcessedMessageAfterGroupingWindow() throws Exception {
        try (KafkaConsumer<Long, ProcessedLinkUpdate> consumer = createProcessedConsumer()) {
            consumer.subscribe(List.of(PROCESSED_TOPIC));
            consumer.poll(Duration.ofMillis(500));

            RawLinkUpdate raw = new RawLinkUpdate(12345L, "critical security patch released", "alice", List.of(111L));
            sendRawUpdate(raw);

            await().atMost(Duration.ofSeconds(15)).pollInterval(Duration.ofMillis(200)).untilAsserted(() -> {
                var records = consumer.poll(Duration.ofMillis(500));
                assertThat(records.count()).isPositive();
                ConsumerRecord<Long, ProcessedLinkUpdate> record = records.iterator().next();
                assertThat(record.value().id()).isEqualTo(12345L);
                assertThat(record.value().description()).isEqualTo(raw.description());
                assertThat(record.value().tgChatIds()).containsExactly(111L);
                assertThat(record.value().priority()).isEqualTo(UpdatePriority.HIGH);
            });
        }
    }

    @Test
    void shouldNotPublishFilteredMessage() throws Exception {
        try (KafkaConsumer<Long, ProcessedLinkUpdate> consumer = createProcessedConsumer()) {
            consumer.subscribe(List.of(PROCESSED_TOPIC));
            consumer.poll(Duration.ofMillis(500));

            RawLinkUpdate raw = new RawLinkUpdate(99L, "spam advertisement here", "alice", List.of(111L));
            sendRawUpdate(raw);

            Thread.sleep(1000);

            var records = consumer.poll(Duration.ofMillis(500));
            assertThat(records.count()).isZero();
        }
    }

    private void sendRawUpdate(RawLinkUpdate update) throws Exception {
        Properties props = producerProps();
        try (KafkaProducer<Long, String> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>(RAW_TOPIC, update.id(), jsonMapper.writeValueAsString(update)))
                    .get();
        }
    }

    private KafkaConsumer<Long, ProcessedLinkUpdate> createProcessedConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        var deserializer = new org.springframework.kafka.support.serializer.JacksonJsonDeserializer<ProcessedLinkUpdate>(
                        JsonMapper.builder().build())
                .copyWithType(ProcessedLinkUpdate.class);
        deserializer.addTrustedPackages("backend.academy.linktracker.ai.dto");
        return new KafkaConsumer<>(props, new LongDeserializer(), deserializer);
    }

    private Properties producerProps() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        return props;
    }
}
