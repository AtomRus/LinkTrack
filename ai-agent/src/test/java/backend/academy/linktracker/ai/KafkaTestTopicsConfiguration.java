package backend.academy.linktracker.ai;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaAdmin;

@TestConfiguration(proxyBeanMethods = false)
public class KafkaTestTopicsConfiguration {

    private static final String RAW_TOPIC = "link.raw-updates";
    private static final String PROCESSED_TOPIC = "link.processed-updates";

    @Bean
    KafkaAdmin kafkaAdmin(org.springframework.core.env.Environment environment) {
        var config = java.util.Map.<String, Object>of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                environment.getProperty("spring.kafka.bootstrap-servers"));
        KafkaAdmin admin = new KafkaAdmin(config);
        admin.setAutoCreate(true);
        return admin;
    }

    @Bean
    NewTopic rawUpdatesTopic() {
        return new NewTopic(RAW_TOPIC, 1, (short) 1);
    }

    @Bean
    NewTopic processedUpdatesTopic() {
        return new NewTopic(PROCESSED_TOPIC, 1, (short) 1);
    }
}
