package backend.academy.linktracker.scrapper.repository.jpa.tag;

import backend.academy.linktracker.scrapper.repository.jpa.model.TagJpa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaTagRepository extends JpaRepository<TagJpa, Long> {
    Optional<TagJpa> findByTag(String tag);
}
