package backend.academy.linktracker.bot.kafka;

import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.dto.ProcessedLinkUpdateEvent;
import backend.academy.linktracker.bot.metrics.BotMetrics;
import backend.academy.linktracker.bot.service.LinkUpdateHandler;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.validation.Validator;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class LinkUpdatesKafkaListenerTest {

    @Mock
    private LinkUpdateHandler linkUpdateHandler;

    private LinkUpdatesKafkaListener listener;

    @BeforeEach
    void setUp() {
        Validator validator = new LocalValidatorFactoryBean();
        ((LocalValidatorFactoryBean) validator).afterPropertiesSet();
        listener = new LinkUpdatesKafkaListener(
                linkUpdateHandler, validator, new SimpleMeterRegistry(), new BotMetrics(new SimpleMeterRegistry()));
    }

    @Test
    void onMessageShouldHandleValidEvent() {
        ProcessedLinkUpdateEvent event =
                new ProcessedLinkUpdateEvent(1L, "description", List.of(10L), "HIGH");
        byte[] emittedAt = ByteBuffer.allocate(Long.BYTES).putLong(System.currentTimeMillis()).array();

        listener.onMessage(event, emittedAt);

        verify(linkUpdateHandler).handleEvent(org.mockito.ArgumentMatchers.argThat(
                dto -> dto.id().equals(1L) && dto.description().equals("description")));
    }
}
