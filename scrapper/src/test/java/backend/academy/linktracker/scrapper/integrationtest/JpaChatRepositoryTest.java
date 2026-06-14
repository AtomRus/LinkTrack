package backend.academy.linktracker.scrapper.integrationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@DirtiesContext
class JpaChatRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ChatRepository chatRepository;

    @Test
    @Transactional
    @Rollback
    void shouldSaveAndFindChat() {
        Chat chat = new Chat(777L);
        chatRepository.save(chat);

        Chat found = chatRepository.findById(777L);

        assertEquals(777L, found.getChatId());
    }
}
