package backend.academy.linktracker.bot.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.linktracker.bot.model.Link;
import backend.academy.linktracker.scrapper.grpc.AddLinkRequest;
import backend.academy.linktracker.scrapper.grpc.GetLinksRequest;
import backend.academy.linktracker.scrapper.grpc.LinkResponse;
import backend.academy.linktracker.scrapper.grpc.ListLinkResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class LinkMapperTest {

    private final LinkMapper mapper = Mappers.getMapper(LinkMapper.class);

    @Test
    void shouldMapToLinkRequest() {
        List<String> tags = new ArrayList<>();
        tags.add("bob");
        Link domainLink = new Link(1L, URI.create("https://google.com"), tags);

        AddLinkRequest request = mapper.toLinkRequest(domainLink, 2L);

        assertThat(request.getLink()).isEqualTo("https://google.com");
        assertThat(request.getTagsList().size()).isNotEqualTo(0);
    }

    @Test
    void shouldMapToLinkFromResponse() {
        LinkResponse response = LinkResponse.newBuilder()
                .setId(100L)
                .setLink("https://github.com")
                .build();

        Link link = mapper.toLink(response);

        assertThat(link.getId()).isEqualTo(100L);
        assertThat(link.getLink()).isEqualTo(URI.create("https://github.com"));
    }

    @Test
    void shouldMapGetLinksRequestWithChatId() {
        Long chatId = 555L;

        GetLinksRequest request = mapper.toGetLinkRequest(chatId);

        assertThat(request.getChatId()).isEqualTo(555L);
    }

    @Test
    void shouldMapListLinkResponseToDomainList() {
        ListLinkResponse response = ListLinkResponse.newBuilder()
                .addLinks(LinkResponse.newBuilder()
                        .setId(1)
                        .setLink("https://link1.com")
                        .build())
                .addLinks(LinkResponse.newBuilder()
                        .setId(2)
                        .setLink("https://link2.com")
                        .build())
                .build();

        List<Link> links = mapper.mapListLinkResponseToListLink(response);

        assertThat(links.size()).isEqualTo(2);
        assertThat(links.get(0).getLink()).isEqualTo(URI.create("https://link1.com"));
        assertThat(links.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void shouldHandleNullUriInMapping() {
        String url = mapper.mapUriToString(null);
        URI uri = mapper.mapStringToUri(null);

        assertThat(url).isNull();
        assertThat(uri).isNull();
    }
}
