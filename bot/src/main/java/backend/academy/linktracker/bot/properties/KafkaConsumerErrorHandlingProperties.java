package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.notifications.kafka")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class KafkaConsumerErrorHandlingProperties {

    @NotBlank
    private String dlqTopic = "link_updates_dlq";

    @Min(0)
    private int retryMaxAttempts = 3;

    @Min(0)
    private long retryBackoffMs = 1000;
}
