package backend.academy.linktracker.scrapper.repository.jpa.chat;

import backend.academy.linktracker.scrapper.repository.jpa.model.ChatJpa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaChatRepository extends JpaRepository<ChatJpa, Long> {
    Optional<ChatJpa> findById(Long chatId);

    @Query("SELECT DISTINCT c FROM ChatJpa c JOIN FETCH c.links l WHERE l.linkUrl = :url")
    List<ChatJpa> findAllByLinkUrl(@Param("url") String url);
}
