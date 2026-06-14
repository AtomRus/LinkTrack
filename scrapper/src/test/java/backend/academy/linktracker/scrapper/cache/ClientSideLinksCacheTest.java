package backend.academy.linktracker.scrapper.cache;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.properties.ValkeyCacheProperties;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClientSideLinksCacheTest {

    @Test
    void shouldReturnEmptyWhenClientSideCacheDisabled() {
        ValkeyCacheProperties props = new ValkeyCacheProperties();
        props.setClientSideCachingEnabled(false);
        ClientSideLinksCache cache = new ClientSideLinksCache(props);

        cache.put(1L, List.of(new Link(1L, 1L, "https://a", null, null, null, List.of())));

        assertThat(cache.get(1L)).isEmpty();
    }

    @Test
    void shouldStoreAndEvictWhenEnabled() {
        ValkeyCacheProperties props = new ValkeyCacheProperties();
        props.setClientSideCachingEnabled(true);
        props.setTtl(Duration.ofMinutes(5));
        ClientSideLinksCache cache = new ClientSideLinksCache(props);
        List<Link> links = List.of(new Link(1L, 10L, "https://a", null, null, null, List.of()));

        cache.put(1L, links);
        assertThat(cache.get(1L)).contains(links);

        cache.evict(1L);
        assertThat(cache.get(1L)).isEmpty();
    }
}
