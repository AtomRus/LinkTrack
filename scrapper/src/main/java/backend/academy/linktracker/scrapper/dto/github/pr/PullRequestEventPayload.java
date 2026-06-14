package backend.academy.linktracker.scrapper.dto.github.pr;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PullRequestEventPayload(
        String action,
        long number,
        @JsonProperty("pull_request") PullRequestDto pullRequest) {}
