package backend.academy.linktracker.loadservice.loadtest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SeedScrapperRequest(
        @Min(1) @Max(500_000) int count,
        @NotNull Long chatId,
        String linkPrefix,
        @Min(1) @Max(512) Integer concurrency) {}
