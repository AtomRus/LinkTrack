package backend.academy.linktracker.scrapper.notification;

import static org.mockito.Mockito.verify;

import backend.academy.linktracker.scrapper.dto.KafkaEvent;
import backend.academy.linktracker.scrapper.outbox.OutboxService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KafkaNotificationSenderTest {

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private KafkaNotificationSender sender;

    @Test
    void sendShouldEnqueueEvent() {
        KafkaEvent event = new KafkaEvent(1L, "https://github.com/a/b", "update", List.of(10L), "author");

        sender.send(event);

        verify(outboxService).enqueue(event);
    }
}
