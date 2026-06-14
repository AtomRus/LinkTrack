package backend.academy.linktracker.scrapper.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Link {
    private Long chatId;
    private Long linkId;
    private String linkUrl;
    private OffsetDateTime lastCheckTime;
    private OffsetDateTime updatedAt;
    private String lastEtag;
    private List<String> tags = new ArrayList<>();

    public List<String> getAllTags() {
        return this.tags != null ? this.tags : new ArrayList<>();
    }
}
