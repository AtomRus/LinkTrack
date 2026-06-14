package backend.academy.linktracker.bot.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.bot.grpc.LinkUpdate;
import backend.academy.linktracker.bot.model.LinkUpdateModel;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class LinkUpdateMapperTest {

    private final LinkUpdateMapper mapper = Mappers.getMapper(LinkUpdateMapper.class);

    @Test
    void toLinkUpdateModel() {
        List<Long> tags = new ArrayList<>();
        tags.add(23L);
        LinkUpdate linkUpdate = LinkUpdate.newBuilder()
                .setId(10L)
                .setDescription("Test description")
                .setUrl("https://link1.co")
                .addAllTgChatIds(tags)
                .build();

        LinkUpdateModel linkUpdateModel = mapper.toLinkUpdateModel(linkUpdate);

        assertThat(linkUpdateModel.getId()).isEqualTo(10L);
        assertThat(linkUpdateModel.getDescription()).isEqualTo("Test description");
        assertThat(linkUpdateModel.getUrl().toString()).isEqualTo("https://link1.co");
        assertThat(linkUpdate.getTgChatIdsList()).isEqualTo(tags);
        assertThat(linkUpdateModel.getTgChatIds()).isEqualTo(tags);
    }
}
