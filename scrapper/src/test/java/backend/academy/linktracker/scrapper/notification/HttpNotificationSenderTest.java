package backend.academy.linktracker.scrapper.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.scrapper.dto.KafkaEvent;
import backend.academy.linktracker.scrapper.properties.NotificationsProperties;
import backend.academy.linktracker.scrapper.properties.ResilienceProperties;
import backend.academy.linktracker.scrapper.resilience.ResilienceExecutor;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.junit.jupiter.api.Test;

class HttpNotificationSenderTest {

    @Test
    void shouldFallbackToKafkaWhenHttpUnavailable() {
        NotificationsProperties notificationsProperties = new NotificationsProperties();
        notificationsProperties.getHttp().setBaseUrl("http://localhost:19090");
        notificationsProperties.getHttp().setUpdatesPath("/updates");

        ResilienceProperties resilienceProperties = new ResilienceProperties();
        resilienceProperties.getNotificationHttp().setTimeoutMs(100);

        KafkaNotificationSender kafkaNotificationSender = mock(KafkaNotificationSender.class);
        ResilienceExecutor resilienceExecutor = mock(ResilienceExecutor.class);
        Retry retry = mock(Retry.class);
        CircuitBreaker circuitBreaker = mock(CircuitBreaker.class);

        HttpNotificationSender sender = new HttpNotificationSender(
                notificationsProperties,
                resilienceProperties,
                kafkaNotificationSender,
                resilienceExecutor,
                retry,
                circuitBreaker);
        KafkaEvent event = new KafkaEvent(1L, "https://example.com", "desc", java.util.List.of(10L), "author");

        doThrow(new RuntimeException("http failed"))
                .when(resilienceExecutor)
                .execute(any(Runnable.class), any(Retry.class), any(CircuitBreaker.class));

        sender.send(event);

        verify(kafkaNotificationSender).send(event);
    }
}
