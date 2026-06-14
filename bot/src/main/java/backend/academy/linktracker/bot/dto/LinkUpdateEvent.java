package backend.academy.linktracker.bot.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record LinkUpdateEvent(
        @NotNull Long id,
        String url,
        @NotNull String description,
        @NotEmpty List<Long> tgChatIds) {}
