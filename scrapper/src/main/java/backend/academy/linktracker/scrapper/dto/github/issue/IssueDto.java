package backend.academy.linktracker.scrapper.dto.github.issue;

import backend.academy.linktracker.scrapper.dto.github.UserDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record IssueDto(
        long id,
        int number,
        String title,
        @JsonProperty("html_url") String htmlUrl,
        UserDto user,
        String body,
        String state,
        List<LabelDto> labels,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("updated_at") OffsetDateTime updatedAt,
        @JsonProperty("closed_at") OffsetDateTime closedAt) {}
