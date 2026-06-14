package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Link;
import java.util.List;
import java.util.Optional;

public interface LinkRepository {
    Link addLink(Link link);

    List<Link> getListOfLinksByChatId(Long chatId);

    List<Link> getListOfLinksByChatIdAndTag(Long chatId, String tag);

    void removeLinkByURL(Long chat, String url);

    void addTagToLink(long linkId, long tagId);

    void updateLink(Link link);

    List<Long> findChatIdsByUrl(String url);

    List<Link> findAll();

    List<Link> findAssignedLinksToUser();

    Optional<Link> findLinkByUrl(String url);

    void removeTagFromLink(long linkId, long tagId);

    List<Link> getListOfLinksByChatId(Long chatId, int limit, int offset);
}
