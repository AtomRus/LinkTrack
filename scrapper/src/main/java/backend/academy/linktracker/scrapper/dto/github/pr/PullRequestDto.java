package backend.academy.linktracker.scrapper.dto.github.pr;

import backend.academy.linktracker.scrapper.dto.github.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PullRequestDto(
        @JsonProperty("id") long id,
        @JsonProperty("title") String title,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("user") UserDto user,
        @JsonProperty("body") String body,
        @JsonProperty("state") String state,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("merged_at") OffsetDateTime mergedAt,
        @JsonProperty("head") PullRequestBranchDto head,
        @JsonProperty("base") PullRequestBranchDto base) {}
