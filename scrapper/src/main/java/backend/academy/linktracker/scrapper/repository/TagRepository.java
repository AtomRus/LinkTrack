package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Tag;
import java.util.Optional;

public interface TagRepository {

    Long addTag(String tag);

    void removeTag(long tagId);

    void updateTag(long tagId, String tag);

    Tag getTag(long tagId);

    Optional<Tag> findByName(String name);
}
