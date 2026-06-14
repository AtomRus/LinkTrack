package backend.academy.linktracker.ai.config;

import backend.academy.linktracker.ai.properties.AiAgentProperties;
import backend.academy.linktracker.ai.summarization.HuggingFaceSummarizer;
import backend.academy.linktracker.ai.summarization.StubSummarizer;
import backend.academy.linktracker.ai.summarization.Summarizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SummarizationConfig {

    @Bean
    @ConditionalOnProperty(
            prefix = "ai-agent.summarization",
            name = "mode",
            havingValue = "stub",
            matchIfMissing = true)
    Summarizer stubSummarizer() {
        return new StubSummarizer();
    }

    @Bean
    @ConditionalOnProperty(prefix = "ai-agent.summarization", name = "mode", havingValue = "ai")
    Summarizer huggingFaceSummarizer(RestClient.Builder restClientBuilder, AiAgentProperties properties) {
        return new HuggingFaceSummarizer(
                restClientBuilder.build(), properties.getSummarization().getHuggingface());
    }
}
