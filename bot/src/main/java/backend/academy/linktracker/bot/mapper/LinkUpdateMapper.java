package backend.academy.linktracker.bot.mapper;

import backend.academy.linktracker.bot.grpc.LinkUpdate;
import backend.academy.linktracker.bot.model.LinkUpdateModel;
import java.net.URI;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LinkUpdateMapper {

    default URI mapStringToUri(String url) {
        return url != null ? URI.create(url) : null;
    }

    default String mapUriToString(URI uri) {
        return uri != null ? uri.toString() : null;
    }

    @Mapping(target = "tgChatIds", source = "tgChatIdsList")
    LinkUpdateModel toLinkUpdateModel(LinkUpdate linkUpdate);
}
