package backend.academy.linktracker.scrapper.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.dto.KafkaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class OutboxServiceTest {

    @Test
    void enqueueShouldPersistNewEvent() {
        OutboxRepository repo = Mockito.mock(OutboxRepository.class);
        ObjectMapper om = new ObjectMapper();
        OutboxService service = new OutboxService(repo, om);

        KafkaEvent evt = new KafkaEvent(1L, "https://e", "d", List.of(10L, 20L), "author");

        service.enqueue(evt);

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        Mockito.verify(repo).save(captor.capture());

        OutboxEventEntity saved = captor.getValue();
        assertThat(saved.getLinkId()).isEqualTo(1L);
        assertThat(saved.getUrl()).isEqualTo("https://e");
        assertThat(saved.getDescription()).isEqualTo("d");
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.NEW);
        assertThat(saved.getTgChatIdsJson()).isEqualTo("[10,20]");
        assertThat(saved.getAuthor()).isEqualTo("author");
    }
}
