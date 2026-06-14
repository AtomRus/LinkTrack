package backend.academy.linktracker.scrapper.notification;

import backend.academy.linktracker.scrapper.dto.KafkaEvent;
import backend.academy.linktracker.scrapper.properties.NotificationsProperties;
import backend.academy.linktracker.scrapper.properties.ResilienceProperties;
import backend.academy.linktracker.scrapper.resilience.ResilienceExecutor;
import backend.academy.linktracker.scrapper.resilience.RetryableHttpException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class HttpNotificationSender implements NotificationSender {
    private final NotificationsProperties notificationsProperties;
    private final KafkaNotificationSender kafkaNotificationSender;
    private final ResilienceExecutor resilienceExecutor;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private final Set<Integer> retryableStatusCodes;
    private final RestClient restClient;

    public HttpNotificationSender(
            NotificationsProperties notificationsProperties,
            ResilienceProperties resilienceProperties,
            KafkaNotificationSender kafkaNotificationSender,
            ResilienceExecutor resilienceExecutor,
            @Qualifier("notificationHttpRetry") Retry retry,
            @Qualifier("notificationHttpCircuitBreaker") CircuitBreaker circuitBreaker) {
        this.notificationsProperties = notificationsProperties;
        this.kafkaNotificationSender = kafkaNotificationSender;
        this.resilienceExecutor = resilienceExecutor;
        this.retry = retry;
        this.circuitBreaker = circuitBreaker;
        this.retryableStatusCodes =
                Set.copyOf(resilienceProperties.getNotificationHttp().getRetry().getRetryableStatusCodes());

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMs = resilienceProperties.getNotificationHttp().getTimeoutMs();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(notificationsProperties.getHttp().getBaseUrl())
                .build();
    }

    @Override
    public void send(KafkaEvent update) {
        try {
            resilienceExecutor.execute(() -> doSend(update), retry, circuitBreaker);
        } catch (Exception ex) {
            log.warn("HTTP notifications unavailable, fallback to Kafka. Reason: {}", ex.getMessage());
            kafkaNotificationSender.send(update);
        }
    }

    private void doSend(KafkaEvent update) {
        try {
            restClient
                    .post()
                    .uri(notificationsProperties.getHttp().getUpdatesPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(update)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            if (retryableStatusCodes.contains(ex.getStatusCode().value())) {
                throw new RetryableHttpException(
                        "Retryable notification status: " + ex.getStatusCode().value(), ex);
            }
            throw ex;
        }
    }
}
