package backend.academy.linktracker.bot.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProcessedLinkUpdateEvent(
        @NotNull Long id,
        @NotNull String description,
        @NotEmpty List<Long> tgChatIds,
        String priority) {}
