package backend.academy.linktracker.scrapper.mapper;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.scrapper.grpc.AddLinkRequest;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.jpa.model.LinkJpa;
import backend.academy.linktracker.scrapper.repository.jpa.model.TagJpa;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkMapperTest {

    private final LinkMapper mapper = Mappers.getMapper(LinkMapper.class);

    @Test
    void shouldMapAddLinkRequestToLink() {
        Long chatId = 123L;
        AddLinkRequest request = AddLinkRequest.newBuilder()
                .setLink("http://test.com")
                .setChatId(chatId)
                .build();

        Link result = mapper.toLink(request, chatId);

        assertNotNull(result);
        assertEquals(chatId, result.getChatId());
        assertEquals("http://test.com", result.getLinkUrl());
    }

    @Test
    void shouldMapEntityToDomain() {
        TagJpa tagJpa = new TagJpa(1L, "news", new HashSet<>());
        LinkJpa entity = new LinkJpa(1L, "http://test.com", null, null, null, Set.of(tagJpa), new HashSet<>());

        Link domain = mapper.toDomain(entity, 123L);

        assertEquals("http://test.com", domain.getLinkUrl());
        assertEquals(1, domain.getTags().size());
        assertTrue(domain.getTags().contains("news"));
    }
}
