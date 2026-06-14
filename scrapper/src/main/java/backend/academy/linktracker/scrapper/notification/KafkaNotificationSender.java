package backend.academy.linktracker.scrapper.notification;

import backend.academy.linktracker.scrapper.dto.KafkaEvent;
import backend.academy.linktracker.scrapper.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaNotificationSender implements NotificationSender {
    private final OutboxService outboxService;

    @Override
    public void send(KafkaEvent update) {
        outboxService.enqueue(update);
    }
}
