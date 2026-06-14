package backend.academy.linktracker.scrapper.integrationtest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@DirtiesContext
class JpaLinkRepositoryWrapperTest extends AbstractIntegrationTest {

    @Autowired
    private LinkRepository linkDAO;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private TagRepository tagRepostiory;

    @Test
    @Transactional
    @Rollback
    void shouldAddLinkAndAssignToChat() {
        long chatId = 10L;
        chatRepository.save(new Chat(chatId));
        Link newLink = new Link(chatId, null, "https://github.com/user/repo", null, null, null, List.of());

        Link savedLink = linkDAO.addLink(newLink);
        chatRepository.addChatLink(chatId, savedLink.getLinkId());

        List<Link> links = linkDAO.getListOfLinksByChatId(chatId);

        assertFalse(links.isEmpty());
        assertEquals("https://github.com/user/repo", links.get(0).getLinkUrl());
    }

    @Test
    @Transactional
    @Rollback
    void shouldAddTagToLink() {
        Link savedLink = linkDAO.addLink(new Link(1L, null, "http://test.com", null, null, null, List.of()));

        long tagId = tagRepostiory.addTag("test_tag");

        assertDoesNotThrow(() -> linkDAO.addTagToLink(savedLink.getLinkId(), tagId));
    }
}
