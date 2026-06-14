package backend.academy.linktracker.scrapper.dto.github.issue;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IssuesEventPayload(
        String action, @JsonProperty("issue") IssueDto issue) {}
