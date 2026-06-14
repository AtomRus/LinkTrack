package backend.academy.linktracker.ai.summarization;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.ai.properties.AiAgentProperties;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@WireMockTest
class HuggingFaceSummarizerTest {

    private HuggingFaceSummarizer summarizer;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wm) {
        AiAgentProperties.HuggingFace huggingFace = new AiAgentProperties.HuggingFace();
        huggingFace.setApiKey("test-token");
        huggingFace.setUrl(wm.getHttpBaseUrl() + "/summarize");

        summarizer = new HuggingFaceSummarizer(RestClient.builder().build(), huggingFace);
    }

    @Test
    void shouldSummarizeLongTextViaApi(WireMockRuntimeInfo wm) {
        stubFor(post(urlEqualTo("/summarize"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"summary_text\":\"Concise summary.\"}]")));

        String text = "a".repeat(600);
        String result = summarizer.summarize(text, 500);

        assertThat(result).isEqualTo("Concise summary.");
        verify(postRequestedFor(urlEqualTo("/summarize")).withHeader("Authorization", containing("Bearer test-token")));
    }

    @Test
    void shouldKeepShortTextWithoutCallingApi() {
        String text = "Short update text";

        String result = summarizer.summarize(text, 500);

        assertThat(result).isEqualTo(text);
        verify(0, postRequestedFor(urlEqualTo("/summarize")));
    }

    @Test
    void shouldFallbackToTruncationWhenApiFails() {
        stubFor(post(urlEqualTo("/summarize")).willReturn(aResponse().withStatus(503)));

        String text = "b".repeat(600);
        String result = summarizer.summarize(text, 500);

        assertThat(result).hasSize(503).endsWith("...");
    }

    @Test
    void shouldExtractSummaryFromGeneratedTextField() throws Exception {
        String summary = HuggingFaceSummarizer.extractSummary("[{\"generated_text\":\"Generated summary\"}]");
        assertThat(summary).isEqualTo("Generated summary");
    }
}
