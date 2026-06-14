package backend.academy.linktracker.scrapper.notification;

import backend.academy.linktracker.scrapper.dto.KafkaEvent;

public interface NotificationSender {
    void send(KafkaEvent update);
}
