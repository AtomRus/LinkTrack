package backend.academy.linktracker.scrapper.integrationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import backend.academy.linktracker.scrapper.model.Tag;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@DirtiesContext
class JpaTagRepositoryWrapperTest extends AbstractIntegrationTest {

    @Autowired
    private TagRepository tagRepository;

    @Test
    @Transactional
    @Rollback
    void shouldAddAndRetrieveTag() {
        Long tagId = tagRepository.addTag("java");
        Tag tag = tagRepository.getTag(tagId);

        assertNotNull(tag);
        assertEquals("java", tag.tag());
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotDuplicateTags() {
        Long id1 = tagRepository.addTag("spring");
        Long id2 = tagRepository.addTag("spring");

        assertEquals(id1, id2, "ID должны быть одинаковыми для одинаковых имен тегов");
    }
}
