package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.cache.ClientSideLinksCache;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.exception.TagNotFoundException;
import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LinkService {
    public static final String LINKS_LIST_CACHE = "${app.cache.valkey.links-list-cache-name}";
    public static final String LINKS_LIST_BY_TAG_CACHE = "${app.cache.valkey.links-list-by-tag-cache-name}";

    private final LinkRepository linkDAO;
    private final TagService tagService;
    private final ChatRepository chatRepository;
    private final ClientSideLinksCache clientSideLinksCache;

    @Transactional
    @Caching(
            evict = {
                @CacheEvict(cacheNames = LINKS_LIST_CACHE, key = "#chatId"),
                @CacheEvict(cacheNames = LINKS_LIST_BY_TAG_CACHE, allEntries = true)
            })
    public void addLink(Long chatId, String url, List<String> tagList) {
        Chat chat = chatRepository.findById(chatId);
        if (chat == null) {
            chat = new Chat(chatId);
            chatRepository.save(chat);
        }
        Link linkEntity = linkDAO.findLinkByUrl(url)
                .orElseGet(() -> linkDAO.addLink(new Link(chatId, null, url, null, null, null, new ArrayList<>())));

        chatRepository.addChatLink(chatId, linkEntity.getLinkId());

        for (String tagName : tagList) {
            Long tagId = tagService.createTag(tagName);
            linkDAO.addTagToLink(linkEntity.getLinkId(), tagId);
        }
        clientSideLinksCache.evict(chatId);
    }

    @Caching(
            evict = {
                @CacheEvict(cacheNames = LINKS_LIST_CACHE, key = "#chatId"),
                @CacheEvict(cacheNames = LINKS_LIST_BY_TAG_CACHE, allEntries = true)
            })
    public void removeLink(Long chatId, String url) {

        linkDAO.removeLinkByURL(chatId, url);
        clientSideLinksCache.evict(chatId);
    }

    @Cacheable(cacheNames = LINKS_LIST_CACHE, key = "#chatId")
    public List<Link> getListOfLinks(Long chatId) {
        return linkDAO.getListOfLinksByChatId(chatId);
    }

    @Cacheable(cacheNames = LINKS_LIST_BY_TAG_CACHE, key = "#chatId + ':' + #tag")
    public List<Link> getListOfLinksByTag(Long chatId, String tag) {
        List<Link> links = linkDAO.getListOfLinksByChatIdAndTag(chatId, tag);
        if (links.isEmpty()) {
            throw new TagNotFoundException("Ссылки с тегом " + tag + " не найдены");
        }
        return links;
    }

    public List<Link> getListOfLinks(Long chatId, int limit, int offset) {
        return linkDAO.getListOfLinksByChatId(chatId, limit, offset);
    }

    public List<Link> getAllLinks() {
        return linkDAO.findAll();
    }

    public List<Link> getAssignedLinksToUser() {
        return linkDAO.findAssignedLinksToUser();
    }

    public List<Long> findChatsByLink(String url) {
        return linkDAO.findChatIdsByUrl(url);
    }

    public void updateLink(Link link) {
        linkDAO.updateLink(link);
    }

    @Transactional
    @Caching(
            evict = {
                @CacheEvict(cacheNames = LINKS_LIST_CACHE, key = "#chatId"),
                @CacheEvict(cacheNames = LINKS_LIST_BY_TAG_CACHE, allEntries = true)
            })
    public void addTagToLink(Long chatId, String url, String tagName) {
        chatRepository.findById(chatId);

        Link link = linkDAO.findLinkByUrl(url)
                .orElseThrow(() -> new LinkNotFoundException("Ссылка не найдена в базе: " + url));

        Long tagId = tagService.createTag(tagName);
        linkDAO.addTagToLink(link.getLinkId(), tagId);
        clientSideLinksCache.evict(chatId);
    }

    @Transactional
    @Caching(
            evict = {
                @CacheEvict(cacheNames = LINKS_LIST_CACHE, key = "#chatId"),
                @CacheEvict(cacheNames = LINKS_LIST_BY_TAG_CACHE, allEntries = true)
            })
    public void removeTagFromLink(Long chatId, String url, String tagName) {
        chatRepository.findById(chatId);

        Link link =
                linkDAO.findLinkByUrl(url).orElseThrow(() -> new LinkNotFoundException("Ссылка не найдена: " + url));

        Tag tag = tagService.getTagByName(tagName);

        linkDAO.removeTagFromLink(link.getLinkId(), tag.tagId());
        clientSideLinksCache.evict(chatId);
    }
}
