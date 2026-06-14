package backend.academy.linktracker.bot.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.bot.configuration.KafkaConsumerConfig;
import backend.academy.linktracker.bot.properties.KafkaConsumerErrorHandlingProperties;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;

class KafkaErrorHandlerConfigTest {

    @Test
    void shouldMarkDeserializationAsNotRetryable() {
        KafkaTemplate<Object, Object> template = org.mockito.Mockito.mock(KafkaTemplate.class);
        KafkaConsumerErrorHandlingProperties props = new KafkaConsumerErrorHandlingProperties();
        props.setDlqTopic("link_updates_dlq");
        props.setRetryMaxAttempts(3);
        props.setRetryBackoffMs(100);

        DefaultErrorHandler handler = new KafkaConsumerConfig().kafkaErrorHandler(template, props);

        assertThat(handler).isNotNull();
        handler.addNotRetryableExceptions(DeserializationException.class);
    }
}
