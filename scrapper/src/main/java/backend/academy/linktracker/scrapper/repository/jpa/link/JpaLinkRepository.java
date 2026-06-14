package backend.academy.linktracker.scrapper.repository.jpa.link;

import backend.academy.linktracker.scrapper.repository.jpa.model.LinkJpa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaLinkRepository extends JpaRepository<LinkJpa, Long> {

    Optional<LinkJpa> findByLinkUrl(String linkUrl);

    Optional<LinkJpa> findByLinkUrlAndChatsChatId(String linkUrl, Long chatId);

    Page<LinkJpa> findByChatsChatId(Long chatId, Pageable pageable);

    @Query("""
    SELECT DISTINCT l as entity, c.chatId as chatId
    FROM LinkJpa l
    JOIN l.chats c
    """)
    List<LinkWithChatIdProjection> findLinkEntitiesWithChatIds();
}
