package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowComment;
import backend.academy.linktracker.scrapper.dto.stackoverflow.TrueStackOverflowAnswer;
import backend.academy.linktracker.scrapper.dto.stackoverflow.TrueStackOverflowResponse;
import backend.academy.linktracker.scrapper.metrics.OperationMetrics;
import backend.academy.linktracker.scrapper.resilience.ResilienceExecutor;
import backend.academy.linktracker.scrapper.resilience.RetryableHttpException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import java.time.OffsetDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@RequiredArgsConstructor
public class StackOverflowClient {
    private final RestClient restClient;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private final Set<Integer> retryableStatusCodes;
    private final ResilienceExecutor resilienceExecutor;
    private final OperationMetrics operationMetrics;

    public TrueStackOverflowResponse<TrueStackOverflowAnswer> fetchAnswers(long questionId, OffsetDateTime since) {
        return operationMetrics.record(
                OperationMetrics.SCOPE_EXTERNAL_SOURCE,
                "stackoverflow.com",
                () -> resilienceExecutor.execute(() -> doFetchAnswers(questionId, since), retry, circuitBreaker));
    }

    public TrueStackOverflowResponse<StackOverflowComment> fetchComments(long questionId, OffsetDateTime since) {
        return operationMetrics.record(
                OperationMetrics.SCOPE_EXTERNAL_SOURCE,
                "stackoverflow.com",
                () -> resilienceExecutor.execute(() -> doFetchComments(questionId, since), retry, circuitBreaker));
    }

    private TrueStackOverflowResponse<TrueStackOverflowAnswer> doFetchAnswers(long questionId, OffsetDateTime since) {
        try {
            return restClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/questions/{id}/answers")
                            .queryParam("order", "desc")
                            .queryParam("sort", "activity")
                            .queryParam("site", "stackoverflow")
                            .queryParam("filter", "withbody")
                            .queryParam("fromdate", since != null ? since.toEpochSecond() : null)
                            .build(questionId))
                    .retrieve()
                    .body(new ParameterizedTypeReference<TrueStackOverflowResponse<TrueStackOverflowAnswer>>() {});
        } catch (RestClientResponseException ex) {
            if (retryableStatusCodes.contains(ex.getStatusCode().value())) {
                throw new RetryableHttpException(
                        "Retryable StackOverflow status: " + ex.getStatusCode().value(), ex);
            }
            throw ex;
        }
    }

    private TrueStackOverflowResponse<StackOverflowComment> doFetchComments(long questionId, OffsetDateTime since) {
        try {
            return restClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/questions/{id}/comments")
                            .queryParam("order", "desc")
                            .queryParam("sort", "creation")
                            .queryParam("site", "stackoverflow")
                            .queryParam("filter", "withbody")
                            .queryParam("fromdate", since != null ? since.toEpochSecond() : null)
                            .build(questionId))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientResponseException ex) {
            if (retryableStatusCodes.contains(ex.getStatusCode().value())) {
                throw new RetryableHttpException(
                        "Retryable StackOverflow status: " + ex.getStatusCode().value(), ex);
            }
            throw ex;
        }
    }
}
