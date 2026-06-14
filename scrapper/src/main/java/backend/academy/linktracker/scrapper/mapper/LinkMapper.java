package backend.academy.linktracker.scrapper.mapper;

import backend.academy.linktracker.scrapper.grpc.AddLinkRequest;
import backend.academy.linktracker.scrapper.grpc.LinkResponse;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.jpa.model.LinkJpa;
import backend.academy.linktracker.scrapper.repository.jpa.model.TagJpa;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LinkMapper {

    default URI mapStringToUri(String url) {
        return url != null ? URI.create(url) : null;
    }

    default String mapUriToString(URI uri) {
        return uri != null ? uri.toString() : null;
    }

    @Mapping(target = "chatId", source = "chatId")
    default Link toLink(AddLinkRequest addLinkRequest, Long chatId) {
        if (addLinkRequest == null && chatId == null) return null;

        Link link = new Link();
        link.setChatId(chatId);

        if (addLinkRequest != null) {
            link.setLinkUrl(addLinkRequest.getLink().toString());

            link.setTags(new ArrayList<>(addLinkRequest.getTagsList()));
        }
        return link;
    }

    default List<LinkResponse> toLinkResponse(List<Link> links) {
        return links.stream()
                .map(linkModel -> LinkResponse.newBuilder()
                        .setId(linkModel.getLinkId())
                        .setLink(linkModel.getLinkUrl())
                        .build())
                .toList();
    }

    @Mapping(target = "tags", ignore = true)
    LinkJpa toEntity(Link link);

    @Mapping(target = "linkUrl", source = "entity.linkUrl")
    @Mapping(target = "chatId", source = "chatId")
    @Mapping(target = "tags", source = "entity.tags")
    Link toDomain(LinkJpa entity, Long chatId);

    default List<String> mapTags(Set<TagJpa> tags) {
        if (tags == null) {
            return null;
        }
        return tags.stream().map(TagJpa::getTag).toList();
    }
}
