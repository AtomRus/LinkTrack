package backend.academy.linktracker.scrapper.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.cache.ClientSideLinksCache;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.service.LinkService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkListControllerTest {

    @Mock
    private LinkService linkService;

    @Mock
    private ClientSideLinksCache clientSideLinksCache;

    @InjectMocks
    private LinkListController controller;

    @Test
    void getLinksShouldReturnCachedLinks() {
        Link link = new Link(100L, 1L, "https://github.com/a/b", null, null, null, List.of("java"));
        when(clientSideLinksCache.get(100L)).thenReturn(Optional.of(List.of(link)));

        var response = controller.getLinks(100L);

        assertThat(response.size()).isEqualTo(1);
        assertThat(response.links().getFirst().url()).isEqualTo("https://github.com/a/b");
    }

    @Test
    void getLinksShouldLoadFromServiceWhenCacheMiss() {
        Link link = new Link(200L, 2L, "https://stackoverflow.com/q/1", null, null, null, List.of());
        when(clientSideLinksCache.get(200L)).thenReturn(Optional.empty());
        when(linkService.getListOfLinks(200L)).thenReturn(List.of(link));

        var response = controller.getLinks(200L);

        assertThat(response.size()).isEqualTo(1);
        verify(clientSideLinksCache).put(200L, List.of(link));
    }
}
