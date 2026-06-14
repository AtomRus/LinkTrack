package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.client.GithubClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.metrics.OperationMetrics;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import backend.academy.linktracker.scrapper.properties.ResilienceProperties;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import backend.academy.linktracker.scrapper.resilience.ResilienceExecutor;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class ClientConfig {
    private final GithubProperties githubProperties;
    private final StackoverflowProperties stackoverflowProperties;
    private final ResilienceProperties resilienceProperties;

    @Bean
    public GithubClient githubClient(
            @Qualifier("githubRetry") Retry retry,
            @Qualifier("githubCircuitBreaker") CircuitBreaker circuitBreaker,
            ResilienceExecutor resilienceExecutor,
            OperationMetrics operationMetrics) {
        RestClient.Builder restClientBuilder = RestClient.builder()
                .requestFactory(
                        timeoutRequestFactory(resilienceProperties.getGithub().getTimeoutMs()))
                .baseUrl(githubProperties.getLink());
        String githubToken = githubProperties.getToken();
        if (!githubToken.isBlank()) {
            restClientBuilder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken);
        }

        return new GithubClient(
                restClientBuilder.build(),
                retry,
                circuitBreaker,
                java.util.Set.copyOf(resilienceProperties.getGithub().getRetry().getRetryableStatusCodes()),
                resilienceExecutor,
                operationMetrics);
    }

    @Bean
    public StackOverflowClient stackOverflowClient(
            @Qualifier("stackoverflowRetry") Retry retry,
            @Qualifier("stackoverflowCircuitBreaker") CircuitBreaker circuitBreaker,
            ResilienceExecutor resilienceExecutor,
            OperationMetrics operationMetrics) {
        RestClient restClient = RestClient.builder()
                .requestFactory(timeoutRequestFactory(
                        resilienceProperties.getStackoverflow().getTimeoutMs()))
                .baseUrl(stackoverflowProperties.getLink())
                .defaultUriVariables(Map.of("key", stackoverflowProperties.getKey()))
                .build();
        return new StackOverflowClient(
                restClient,
                retry,
                circuitBreaker,
                java.util.Set.copyOf(
                        resilienceProperties.getStackoverflow().getRetry().getRetryableStatusCodes()),
                resilienceExecutor,
                operationMetrics);
    }

    private SimpleClientHttpRequestFactory timeoutRequestFactory(int timeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);
        return requestFactory;
    }
}
