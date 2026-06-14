package backend.academy.linktracker.scrapper.notification;

import static org.mockito.Mockito.verify;

import backend.academy.linktracker.scrapper.dto.KafkaEvent;
import backend.academy.linktracker.scrapper.grpc.BotServiceGrpc;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GrpcNotificationSenderTest {

    @Mock
    private BotServiceGrpc botServiceGrpc;

    @InjectMocks
    private GrpcNotificationSender sender;

    @Test
    void sendShouldDelegateToBotServiceGrpc() {
        KafkaEvent event = new KafkaEvent(7L, "https://github.com/x/y", "desc", List.of(1L, 2L), "author");

        sender.send(event);

        verify(botServiceGrpc).sendMessage(7L, URI.create("https://github.com/x/y"), "desc", List.of(1L, 2L));
    }
}
