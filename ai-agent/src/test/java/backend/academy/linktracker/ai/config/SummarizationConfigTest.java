package backend.academy.linktracker.ai.config;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.ai.properties.AiAgentProperties;
import backend.academy.linktracker.ai.summarization.HuggingFaceSummarizer;
import backend.academy.linktracker.ai.summarization.StubSummarizer;
import backend.academy.linktracker.ai.summarization.Summarizer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

class SummarizationConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SummarizationConfig.class)
            .withBean(RestClient.Builder.class, RestClient::builder)
            .withBean(AiAgentProperties.class, AiAgentProperties::new);

    @Test
    void shouldUseStubSummarizerByDefault() {
        contextRunner.run(
                context -> assertThat(context.getBean(Summarizer.class)).isInstanceOf(StubSummarizer.class));
    }

    @Test
    void shouldUseHuggingFaceSummarizerWhenModeIsAi() {
        contextRunner.withPropertyValues("ai-agent.summarization.mode=ai").run(context -> assertThat(
                        context.getBean(Summarizer.class))
                .isInstanceOf(HuggingFaceSummarizer.class));
    }
}
