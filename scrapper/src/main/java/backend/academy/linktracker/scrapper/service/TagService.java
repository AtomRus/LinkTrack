package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.exception.TagNotFoundException;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public Long createTag(String tagName) {
        return tagRepository.addTag(tagName);
    }

    public Tag getTag(long tagId) {
        Tag tag = tagRepository.getTag(tagId);
        if (tag == null) {
            throw new TagNotFoundException("Тег с ID " + tagId + " не найден");
        }
        return tag;
    }

    public Tag getTagByName(String tagName) {
        return tagRepository
                .findByName(tagName)
                .orElseThrow(() -> new TagNotFoundException("Тег с названием '" + tagName + "' не найден"));
    }

    public void updateTag(long tagId, String newTagName) {
        getTag(tagId);
        tagRepository.updateTag(tagId, newTagName);
    }

    public void deleteTag(long tagId) {
        getTag(tagId);
        tagRepository.removeTag(tagId);
    }
}
