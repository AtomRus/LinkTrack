package backend.academy.linktracker.ai.summarization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StubSummarizerTest {

    private final StubSummarizer summarizer = new StubSummarizer();

    @Test
    void shouldSummarizeLongText() {
        String text = "a".repeat(600);

        String result = summarizer.summarize(text, 500);

        assertThat(result).hasSize(503);
        assertThat(result).endsWith("...");
        assertThat(result).isNotEqualTo(text);
    }

    @Test
    void shouldKeepShortTextUnchanged() {
        String text = "Short update text";

        String result = summarizer.summarize(text, 500);

        assertThat(result).isEqualTo(text);
    }
}
