package backend.academy.linktracker.loadservice.loadtest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BotRegistrationLoadRequest(
        @NotBlank String botBaseUrl,
        @Min(1) @Max(500_000) int iterations,
        @NotNull Long chatId,
        String linkPrefix,
        @Min(1) @Max(512) Integer concurrency) {}
