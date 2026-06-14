package backend.academy.linktracker.loadservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    RestClient githubRestClient(
            @Value("${load.github.api-base:https://api.github.com}") String baseUrl,
            @Value("${GITHUB_TOKEN:}") String token) {
        var builder = RestClient.builder().baseUrl(baseUrl);
        if (token != null && !token.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return builder.build();
    }

    @Bean
    RestClient stackExchangeRestClient(
            @Value("${load.stackexchange.api-base:https://api.stackexchange.com/2.3}") String baseUrl,
            @Value("${STACKOVERFLOW_KEY:}") String key) {

        var builder = RestClient.builder().baseUrl(baseUrl);

        return builder.build();
    }
}
