package backend.academy.linktracker.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowUser(
        @JsonProperty("display_name") String displayName,
        @JsonProperty("user_id") Long userId,
        String link) {}
