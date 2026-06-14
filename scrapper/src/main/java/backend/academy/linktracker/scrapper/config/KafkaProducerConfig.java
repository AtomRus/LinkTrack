package backend.academy.linktracker.scrapper.config;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    public static final String GENERIC_KAFKA_TEMPLATE_BEAN = "genericKafkaTemplate";

    private final KafkaProperties properties;

    @Bean(GENERIC_KAFKA_TEMPLATE_BEAN)
    public KafkaTemplate<Long, Object> genericKafkaTemplate() {
        var props = properties.buildProducerProperties();

        // Сериализация
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        var factory = new DefaultKafkaProducerFactory<Long, Object>(props);
        return new KafkaTemplate<>(factory);
    }
}
