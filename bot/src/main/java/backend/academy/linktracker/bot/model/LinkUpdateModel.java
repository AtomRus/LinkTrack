package backend.academy.linktracker.bot.model;

import java.net.URI;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkUpdateModel {
    private Long id;
    private URI url;
    private String description;
    private List<Long> tgChatIds;
}
