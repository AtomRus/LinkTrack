package backend.academy.linktracker.scrapper.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@RequiredArgsConstructor
public class RawUpdatesKafkaProducerConfig {

    public static final String RAW_UPDATES_KAFKA_TEMPLATE_BEAN = "rawUpdatesKafkaTemplate";

    private final KafkaProperties properties;

    @Bean(RAW_UPDATES_KAFKA_TEMPLATE_BEAN)
    public KafkaTemplate<Long, String> rawUpdatesKafkaTemplate() {
        var props = properties.buildProducerProperties();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        var factory = new DefaultKafkaProducerFactory<Long, String>(props);
        KafkaTemplate<Long, String> template = new KafkaTemplate<>(factory);
        template.setObservationEnabled(true);
        return template;
    }
}
