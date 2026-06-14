package backend.academy.linktracker.scrapper.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.cache.ClientSideLinksCache;
import backend.academy.linktracker.scrapper.exception.TagNotFoundException;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    @Mock
    private LinkRepository linkDAO;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private TagService tagService;

    @Mock
    private ClientSideLinksCache clientSideLinksCache;

    @InjectMocks
    private LinkService linkService;

    @Test
    void addLink_ShouldCreateChatAndPersistLink_WhenChatMissing() {
        Long chatId = 1L;
        String url = "http://google.com";
        when(chatRepository.findById(chatId)).thenReturn(null);
        when(linkDAO.findLinkByUrl(url)).thenReturn(java.util.Optional.empty());
        when(linkDAO.addLink(any())).thenAnswer(inv -> inv.getArgument(0));

        linkService.addLink(chatId, url, List.of());

        verify(chatRepository).save(any());
        verify(linkDAO).addLink(any());
    }

    @Test
    void removeLinkShouldEvictCache() {
        linkService.removeLink(1L, "https://example.com");
        verify(linkDAO).removeLinkByURL(1L, "https://example.com");
        verify(clientSideLinksCache).evict(1L);
    }

    @Test
    void getListOfLinksByTagShouldThrowWhenEmpty() {
        when(linkDAO.getListOfLinksByChatIdAndTag(1L, "missing")).thenReturn(List.of());

        assertThrows(TagNotFoundException.class, () -> linkService.getListOfLinksByTag(1L, "missing"));
    }

    @Test
    void addTagToLinkShouldCreateTagAndLinkIt() {
        Link link = new Link(1L, 10L, "https://example.com", null, null, null, List.of());
        when(linkDAO.findLinkByUrl("https://example.com")).thenReturn(java.util.Optional.of(link));
        when(tagService.createTag("java")).thenReturn(5L);

        linkService.addTagToLink(1L, "https://example.com", "java");

        verify(linkDAO).addTagToLink(10L, 5L);
        verify(clientSideLinksCache).evict(1L);
    }
}
