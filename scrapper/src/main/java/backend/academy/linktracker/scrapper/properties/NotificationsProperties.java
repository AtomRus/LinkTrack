package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.notifications")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class NotificationsProperties {

    /**
     * kafka | grpc | http
     */
    @NotBlank
    private String transport = "kafka";

    @Valid
    private Kafka kafka = new Kafka();

    @Valid
    private Http http = new Http();

    @Valid
    private Outbox outbox = new Outbox();

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class Kafka {
        @NotBlank
        private String topic = "link_updates";

        @NotBlank
        private String rawTopic = "link.raw-updates";
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class Http {
        @NotBlank
        private String baseUrl = "http://localhost:8080";

        @NotBlank
        private String updatesPath = "/updates";
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class Outbox {
        @Positive
        private int batchSize = 50;

        @Positive
        private int maxAttempts = 10;

        @Positive
        private long sendTimeoutMs = 10000;
    }
}
