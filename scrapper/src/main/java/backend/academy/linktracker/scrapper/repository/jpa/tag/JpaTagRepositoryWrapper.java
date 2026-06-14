package backend.academy.linktracker.scrapper.repository.jpa.tag;

import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import backend.academy.linktracker.scrapper.repository.jpa.model.TagJpa;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jpa")
public class JpaTagRepositoryWrapper implements TagRepository {

    private final JpaTagRepository jpaTagRepository;

    @Override
    @Transactional
    public Long addTag(String tagName) {
        return jpaTagRepository.findByTag(tagName).map(TagJpa::getTagId).orElseGet(() -> {
            TagJpa newTag = new TagJpa();
            newTag.setTag(tagName);
            return jpaTagRepository.save(newTag).getTagId();
        });
    }

    @Override
    @Transactional
    public void removeTag(long tagId) {
        jpaTagRepository.deleteById(tagId);
    }

    @Override
    @Transactional
    public void updateTag(long tagId, String tagName) {
        jpaTagRepository.findById(tagId).ifPresent(tagJpa -> {
            tagJpa.setTag(tagName);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Tag getTag(long tagId) {
        return jpaTagRepository
                .findById(tagId)
                .map(tagJpa -> new Tag(tagJpa.getTagId(), tagJpa.getTag()))
                .orElse(null);
    }

    @Override
    public Optional<Tag> findByName(String name) {
        return jpaTagRepository.findByTag(name).map(entity -> new Tag(entity.getTagId(), entity.getTag()));
    }
}
