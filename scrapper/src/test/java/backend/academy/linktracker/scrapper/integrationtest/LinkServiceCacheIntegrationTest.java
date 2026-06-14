package backend.academy.linktracker.scrapper.integrationtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.LinkService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@DirtiesContext
class LinkServiceCacheIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private LinkService linkService;

    @MockitoSpyBean
    private LinkRepository linkRepository;

    @Test
    void shouldUseCacheForRepeatedGetListCalls() {
        Long chatId = 2001L;
        linkService.addLink(chatId, "https://example.com/cache-hit", List.of("cache"));
        clearInvocations(linkRepository);

        var first = linkService.getListOfLinks(chatId);
        var second = linkService.getListOfLinks(chatId);

        assertThat(first).isNotEmpty();
        assertThat(second).hasSize(first.size());
        assertThat(second).extracting("linkUrl").containsExactlyInAnyOrderElementsOf(
                first.stream().map(link -> link.getLinkUrl()).toList());
    }

    @Test
    @Transactional
    void shouldEvictCacheAfterAddOrRemoveLink() {
        Long chatId = 2002L;
        linkService.addLink(chatId, "https://example.com/first", List.of());

        // Прогреваем кэш и проверяем, что репозиторий вызван один раз
        clearInvocations(linkRepository);
        linkService.getListOfLinks(chatId);
        verify(linkRepository, times(1)).getListOfLinksByChatId(chatId);

        // Мутация должна инвалидировать кэш для этого чата
        linkService.addLink(chatId, "https://example.com/second", List.of());
        clearInvocations(linkRepository);
        var linksAfterAdd = linkService.getListOfLinks(chatId);
        verify(linkRepository, times(1)).getListOfLinksByChatId(chatId);
        assertThat(linksAfterAdd).hasSizeGreaterThanOrEqualTo(2);

        // Следующая мутация снова должна инвалидировать кэш
        linkService.removeLink(chatId, "https://example.com/second");
        clearInvocations(linkRepository);
        var linksAfterRemove = linkService.getListOfLinks(chatId);
        verify(linkRepository, times(1)).getListOfLinksByChatId(chatId);
        assertThat(linksAfterRemove).allMatch(link -> !"https://example.com/second".equals(link.getLinkUrl()));
    }
}
