package backend.academy.linktracker.bot.mapper;

import backend.academy.linktracker.bot.model.Link;
import backend.academy.linktracker.scrapper.grpc.AddLinkRequest;
import backend.academy.linktracker.scrapper.grpc.GetLinksRequest;
import backend.academy.linktracker.scrapper.grpc.LinkResponse;
import backend.academy.linktracker.scrapper.grpc.ListLinkResponse;
import backend.academy.linktracker.scrapper.grpc.RemoveLinkRequest;
import java.net.URI;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LinkMapper {
    default URI mapStringToUri(String url) {
        return url != null ? URI.create(url) : null;
    }

    default String mapUriToString(URI uri) {
        return uri != null ? uri.toString() : null;
    }

    default List<Link> mapListLinkResponseToListLink(ListLinkResponse listLinkResponse) {
        return listLinkResponse.getLinksList().stream()
                .map(linkResponse ->
                        new Link(linkResponse.getId(), URI.create(linkResponse.getLink()), linkResponse.getTagsList()))
                .toList();
    }

    default AddLinkRequest toLinkRequest(Link link, Long chatId) {
        if (link.getTags() == null) {
            return AddLinkRequest.newBuilder()
                    .setChatId(chatId)
                    .setLink(link.getLink().toString())
                    .build();
        }
        return AddLinkRequest.newBuilder()
                .setChatId(chatId)
                .setLink(link.getLink().toString())
                .addAllTags(link.getTags())
                .build();
    }

    Link toLink(LinkResponse linkResponse);

    RemoveLinkRequest toRemoveLinkRequest(Link link);

    @Mapping(source = "chatId", target = "chatId")
    GetLinksRequest toGetLinkRequest(Long chatId);
}
