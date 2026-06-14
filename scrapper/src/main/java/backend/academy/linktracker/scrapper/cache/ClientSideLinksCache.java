package backend.academy.linktracker.scrapper.cache;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.properties.ValkeyCacheProperties;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ClientSideLinksCache {
    private final Map<Long, LocalEntry> localCache = new ConcurrentHashMap<>();
    private final ValkeyCacheProperties valkeyCacheProperties;

    public ClientSideLinksCache(ValkeyCacheProperties valkeyCacheProperties) {
        this.valkeyCacheProperties = valkeyCacheProperties;
    }

    public Optional<List<Link>> get(Long chatId) {
        if (!valkeyCacheProperties.isClientSideCachingEnabled()) {
            return Optional.empty();
        }
        LocalEntry entry = localCache.get(chatId);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expiresAt().isBefore(Instant.now())) {
            localCache.remove(chatId);
            return Optional.empty();
        }
        return Optional.of(entry.links());
    }

    public void put(Long chatId, List<Link> links) {
        if (!valkeyCacheProperties.isClientSideCachingEnabled()) {
            return;
        }
        localCache.put(chatId, new LocalEntry(List.copyOf(links), Instant.now().plus(valkeyCacheProperties.getTtl())));
    }

    public void evict(Long chatId) {
        localCache.remove(chatId);
    }

    private record LocalEntry(List<Link> links, Instant expiresAt) {}
}
