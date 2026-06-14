package backend.academy.linktracker.bot.model;

import java.net.URI;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Link {
    private Long id;
    private URI link;
    private List<String> tags;
}
