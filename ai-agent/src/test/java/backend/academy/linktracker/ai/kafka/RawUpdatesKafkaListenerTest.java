package backend.academy.linktracker.ai.kafka;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import backend.academy.linktracker.ai.dto.RawLinkUpdate;
import backend.academy.linktracker.ai.service.UpdateProcessingService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class RawUpdatesKafkaListenerTest {

    @Mock
    private UpdateProcessingService updateProcessingService;

    @InjectMocks
    private RawUpdatesKafkaListener listener;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void shouldProcessValidPayload() throws Exception {
        RawUpdatesKafkaListener wired = new RawUpdatesKafkaListener(jsonMapper, updateProcessingService);
        String payload = jsonMapper.writeValueAsString(new RawLinkUpdate(1L, "desc", "author", List.of(10L)));

        wired.onMessage(payload);

        verify(updateProcessingService).process(new RawLinkUpdate(1L, "desc", "author", List.of(10L)));
    }

    @Test
    void shouldIgnoreInvalidPayload() {
        listener.onMessage("{invalid-json");

        verifyNoInteractions(updateProcessingService);
    }
}
