package backend.academy.linktracker.scrapper.repository.jpa.link;

import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.exception.ResourceNotFoundException;
import backend.academy.linktracker.scrapper.exception.TagNotFoundException;
import backend.academy.linktracker.scrapper.mapper.LinkMapper;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.jpa.chat.JpaChatRepository;
import backend.academy.linktracker.scrapper.repository.jpa.model.ChatJpa;
import backend.academy.linktracker.scrapper.repository.jpa.model.LinkJpa;
import backend.academy.linktracker.scrapper.repository.jpa.model.TagJpa;
import backend.academy.linktracker.scrapper.repository.jpa.tag.JpaTagRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jpa")
public class JpaLinkRepositoryWrapper implements LinkRepository {
    private final JpaLinkRepository jpaLinkRepository;
    private final JpaChatRepository jpaChatRepository;
    private final JpaTagRepository jpaTagRepository;
    private final LinkMapper linkMapper;

    @Override
    public Link addLink(Link link) {
        LinkJpa entity = linkMapper.toEntity(link);

        LinkJpa savedEntity = jpaLinkRepository.save(entity);

        return linkMapper.toDomain(savedEntity, link.getChatId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Link> getListOfLinksByChatId(Long chatId) {
        return jpaChatRepository
                .findById(chatId)
                .map(chat -> chat.getLinks().stream()
                        .map(linkJpa -> new Link(
                                chatId, linkJpa.getLinkId(), linkJpa.getLinkUrl(), null, null, null, new ArrayList<>()))
                        .toList())
                .orElse(List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Link> getListOfLinksByChatIdAndTag(Long chatId, String tagName) {
        return jpaChatRepository
                .findById(chatId)
                .map(chat -> chat.getLinks().stream()
                        .filter(link -> link.getTags().stream()
                                .anyMatch(tag -> tag.getTag().equalsIgnoreCase(tagName)))
                        .map(linkJpa -> new Link(
                                chatId, linkJpa.getLinkId(), linkJpa.getLinkUrl(), null, null, null, new ArrayList<>()))
                        .toList())
                .orElse(List.of());
    }

    @Override
    @Transactional
    public void removeLinkByURL(Long chatId, String url) {
        ChatJpa chatJpa = jpaChatRepository
                .findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Чат с ID " + chatId + " не найден"));

        LinkJpa linkJpa = jpaLinkRepository
                .findByLinkUrlAndChatsChatId(url, chatId)
                .orElseThrow(() -> new LinkNotFoundException("У пользователя нет такой ссылки: " + url));

        chatJpa.getLinks().remove(linkJpa);
        linkJpa.getChats().remove(chatJpa);
        jpaChatRepository.save(chatJpa);

        if (linkJpa.getChats().isEmpty()) {
            jpaLinkRepository.delete(linkJpa);
        }
    }

    @Override
    @Transactional
    public void addTagToLink(long linkId, long tagId) {
        LinkJpa link = jpaLinkRepository
                .findById(linkId)
                .orElseThrow(() -> new LinkNotFoundException("Ссылка с ID " + linkId + " не найдена"));

        TagJpa tag = jpaTagRepository
                .findById(tagId)
                .orElseThrow(() -> new TagNotFoundException("Тег с ID " + tagId + " не найден"));

        link.getTags().add(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findChatIdsByUrl(String url) {
        return jpaLinkRepository
                .findByLinkUrl(url)
                .map(link -> link.getChats().stream().map(ChatJpa::getChatId).toList())
                .orElse(List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Link> findAll() {
        return jpaLinkRepository.findLinkEntitiesWithChatIds().stream()
                .map(projection -> linkMapper.toDomain(projection.getEntity(), projection.getChatId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Link> findLinkByUrl(String url) {
        return jpaLinkRepository.findByLinkUrl(url).map(linkJpa -> linkMapper.toDomain(linkJpa, null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Link> findAssignedLinksToUser() {
        return jpaLinkRepository.findLinkEntitiesWithChatIds().stream()
                .map(projection -> linkMapper.toDomain(projection.getEntity(), projection.getChatId()))
                .toList();
    }

    @Override
    @Transactional
    public void updateLink(Link link) {
        LinkJpa entity = jpaLinkRepository
                .findById(link.getLinkId())
                .orElseThrow(() -> new LinkNotFoundException(String.valueOf(link.getLinkId())));

        entity.setLastCheckTime(link.getLastCheckTime());
        entity.setUpdatedAt(link.getUpdatedAt());

        jpaLinkRepository.save(entity);
    }

    @Override
    @Transactional
    public void removeTagFromLink(long linkId, long tagId) {
        LinkJpa link =
                jpaLinkRepository.findById(linkId).orElseThrow(() -> new LinkNotFoundException("Ссылка не найдена"));

        link.getTags().removeIf(tag -> tag.getTagId().equals(tagId));
        jpaLinkRepository.save(link);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Link> getListOfLinksByChatId(Long chatId, int limit, int offset) {
        int page = offset / limit;

        return jpaLinkRepository.findByChatsChatId(chatId, PageRequest.of(page, limit)).stream()
                .map(linkJpa -> new Link(
                        chatId, linkJpa.getLinkId(), linkJpa.getLinkUrl(), null, null, null, new ArrayList<>()))
                .toList();
    }
}
