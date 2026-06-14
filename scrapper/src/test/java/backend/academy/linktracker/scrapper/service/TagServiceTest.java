package backend.academy.linktracker.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.exception.TagNotFoundException;
import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    void createTagShouldDelegateToRepository() {
        when(tagRepository.addTag("java")).thenReturn(42L);

        assertThat(tagService.createTag("java")).isEqualTo(42L);
    }

    @Test
    void getTagShouldThrowWhenMissing() {
        when(tagRepository.getTag(1L)).thenReturn(null);

        assertThatThrownBy(() -> tagService.getTag(1L)).isInstanceOf(TagNotFoundException.class);
    }

    @Test
    void getTagByNameShouldReturnTag() {
        Tag tag = new Tag(1L, "spring");
        when(tagRepository.findByName("spring")).thenReturn(Optional.of(tag));

        assertThat(tagService.getTagByName("spring")).isEqualTo(tag);
    }

    @Test
    void updateAndDeleteShouldValidateExistence() {
        Tag tag = new Tag(5L, "old");
        when(tagRepository.getTag(5L)).thenReturn(tag);

        tagService.updateTag(5L, "new");
        tagService.deleteTag(5L);

        verify(tagRepository).updateTag(5L, "new");
        verify(tagRepository).removeTag(5L);
    }
}
