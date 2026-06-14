package backend.academy.linktracker.scrapper.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class KafkaEvent {
    long id;
    String url;
    String description;
    List<Long> tgChatIds;
    String author;
}
