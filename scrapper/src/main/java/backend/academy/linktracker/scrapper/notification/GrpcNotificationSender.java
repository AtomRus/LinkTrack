package backend.academy.linktracker.scrapper.notification;

import backend.academy.linktracker.scrapper.dto.KafkaEvent;
import backend.academy.linktracker.scrapper.grpc.BotServiceGrpc;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrpcNotificationSender implements NotificationSender {
    private final BotServiceGrpc botServiceGrpc;

    @Override
    public void send(KafkaEvent update) {
        botServiceGrpc.sendMessage(
                update.getId(), URI.create(update.getUrl()), update.getDescription(), update.getTgChatIds());
    }
}
