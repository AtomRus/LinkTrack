package backend.academy.linktracker.bot.service;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.bot.model.Link;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LinkSessionServiceTest {

    private LinkSessionService linkSessionService;

    @BeforeEach
    void setUp() {
        linkSessionService = new LinkSessionService();
    }

    @Test
    @DisplayName("Должен сохранять и возвращать сессию")
    void shouldUpdateAndGetSession() {
        Long chatId = 1L;
        Link link = new Link(chatId, URI.create("https://example.com"), List.of("test"));

        linkSessionService.updateSession(link);
        Link retrievedLink = linkSessionService.getSession(chatId);

        assertNotNull(retrievedLink);
        assertEquals(link.getLink(), retrievedLink.getLink());
        assertEquals(link.getTags(), retrievedLink.getTags());
    }

    @Test
    @DisplayName("Должен возвращать null, если сессии нет")
    void shouldReturnNullWhenSessionDoesNotExist() {
        assertNull(linkSessionService.getSession(999L));
    }

    @Test
    @DisplayName("Должен удалять черновик")
    void shouldDeleteDraft() {
        Long chatId = 2L;
        Link link = new Link(chatId, URI.create("https://example.com"), List.of("test"));

        linkSessionService.updateSession(link);
        assertNotNull(linkSessionService.getSession(chatId));

        linkSessionService.deleteDraft(chatId);
        assertNull(linkSessionService.getSession(chatId));
    }
}
