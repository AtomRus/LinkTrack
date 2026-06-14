package backend.academy.linktracker.ai.summarization;

import backend.academy.linktracker.ai.properties.AiAgentProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Slf4j
@RequiredArgsConstructor
public class HuggingFaceSummarizer implements Summarizer {

    private static final String PROMPT_TEMPLATE = "Summarize the following update in 2-3 sentences:\n\n%s";

    private final RestClient restClient;
    private final AiAgentProperties.HuggingFace huggingFace;

    @Override
    public String summarize(String text, int threshold) {
        if (text.length() <= threshold) {
            return text;
        }
        try {
            String responseBody = restClient
                    .post()
                    .uri(huggingFace.getUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + huggingFace.getApiKey())
                    .body(Map.of("inputs", PROMPT_TEMPLATE.formatted(text)))
                    .retrieve()
                    .body(String.class);

            String summary = extractSummary(responseBody);
            if (summary == null || summary.isBlank()) {
                log.warn("Hugging Face вернул пустую суммаризацию, используется обрезка текста");
                return truncate(text, threshold);
            }
            return summary.trim();
        } catch (Exception ex) {
            log.warn("Ошибка суммаризации Hugging Face: {}, используется обрезка текста", ex.getMessage());
            return truncate(text, threshold);
        }
    }

    static String extractSummary(String responseBody) throws com.fasterxml.jackson.core.JsonProcessingException {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);
        if (root.isArray() && !root.isEmpty()) {
            JsonNode first = root.get(0);
            if (first.has("summary_text")) {
                return first.get("summary_text").asText();
            }
            if (first.has("generated_text")) {
                return first.get("generated_text").asText();
            }
        }
        if (root.has("generated_text")) {
            return root.get("generated_text").asText();
        }
        if (root.isTextual()) {
            return root.asText();
        }
        return null;
    }

    private static String truncate(String text, int threshold) {
        return text.substring(0, threshold) + "...";
    }
}
