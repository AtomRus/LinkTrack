package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.response.GithubEventResponse;
import backend.academy.linktracker.scrapper.metrics.OperationMetrics;
import backend.academy.linktracker.scrapper.resilience.ResilienceExecutor;
import backend.academy.linktracker.scrapper.resilience.RetryableHttpException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@RequiredArgsConstructor
public class GithubClient {
    private final RestClient restClient;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private final Set<Integer> retryableStatusCodes;
    private final ResilienceExecutor resilienceExecutor;
    private final OperationMetrics operationMetrics;

    public ResponseEntity<List<GithubEventResponse>> fetchEvents(String owner, String repo, String lastEtag) {
        return operationMetrics.record(
                OperationMetrics.SCOPE_EXTERNAL_SOURCE,
                "github.com",
                () -> resilienceExecutor.execute(() -> doFetchEvents(owner, repo, lastEtag), retry, circuitBreaker));
    }

    private ResponseEntity<List<GithubEventResponse>> doFetchEvents(String owner, String repo, String lastEtag) {
        try {
            var requestSpec = restClient.get().uri("/repos/{owner}/{repo}/events", owner, repo);
            if (lastEtag != null && !lastEtag.isBlank()) {
                requestSpec = requestSpec.header("If-None-Match", lastEtag);
            }

            return requestSpec.retrieve().toEntity(new ParameterizedTypeReference<>() {});
        } catch (RestClientResponseException ex) {
            if (retryableStatusCodes.contains(ex.getStatusCode().value())) {
                throw new RetryableHttpException(
                        "Retryable GitHub status: " + ex.getStatusCode().value(), ex);
            }
            throw ex;
        }
    }
}
