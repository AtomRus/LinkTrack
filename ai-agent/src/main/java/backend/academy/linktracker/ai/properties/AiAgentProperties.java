package backend.academy.linktracker.ai.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "ai-agent")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class AiAgentProperties {

    @Valid
    private Filtering filtering = new Filtering();

    @Valid
    private Summarization summarization = new Summarization();

    @Valid
    private Prioritization prioritization = new Prioritization();

    @Valid
    private Grouping grouping = new Grouping();

    @Valid
    private Kafka kafka = new Kafka();

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class Filtering {
        private List<String> stopWords = new ArrayList<>();
        private List<String> excludedAuthors = new ArrayList<>();

        @Min(0)
        private int minLength = 0;
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class Summarization {
        /**
         * stub — обрезка до порога; ai — Hugging Face Inference API.
         */
        @NotBlank
        private String mode = "stub";

        @Min(1)
        private int threshold = 500;

        @Valid
        private HuggingFace huggingface = new HuggingFace();
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class HuggingFace {
        private String apiKey;
        private String url = "https://api-inference.huggingface.co/models/facebook/bart-large-cnn";
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class Prioritization {
        private List<String> highKeywords = new ArrayList<>();
        private List<String> lowKeywords = new ArrayList<>();
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class Grouping {
        @Min(1)
        private long windowMs = 30_000;
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class Kafka {
        @NotBlank
        private String rawTopic = "link.raw-updates";

        @NotBlank
        private String processedTopic = "link.processed-updates";

        @NotBlank
        private String groupId = "ai-agent";
    }
}
