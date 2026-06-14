package backend.academy.linktracker.loadservice.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LoadRunRequest(
        @NotNull Source source, @Min(1) @Max(500) int count, long chatId, String githubQuery) {

    public enum Source {
        github,
        stackoverflow
    }
}
