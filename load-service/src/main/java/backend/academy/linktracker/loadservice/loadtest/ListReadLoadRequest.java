package backend.academy.linktracker.loadservice.loadtest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ListReadLoadRequest(
        @NotNull Long chatId,
        @NotNull @Min(1) Integer iterations,
        @NotNull @Min(1) Integer concurrency,
        Integer warmupIterations) {}
