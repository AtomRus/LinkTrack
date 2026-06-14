package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.properties.KafkaConsumerErrorHandlingProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.DeserializationException;

@Configuration
@EnableConfigurationProperties(KafkaConsumerErrorHandlingProperties.class)
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate, KafkaConsumerErrorHandlingProperties props) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (ConsumerRecord<?, ?> record, Exception ex) ->
                        new TopicPartition(props.getDlqTopic(), record.partition()));

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(props.getRetryMaxAttempts());
        backOff.setInitialInterval(props.getRetryBackoffMs());
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(Math.max(props.getRetryBackoffMs(), 10_000));

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        handler.addNotRetryableExceptions(DeserializationException.class, SerializationException.class);

        handler.addNotRetryableExceptions(jakarta.validation.ConstraintViolationException.class);

        return handler;
    }
}
