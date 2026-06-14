package backend.academy.linktracker.bot.loadtest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RegisterLinkRequest(
        @NotNull Long chatId, @NotBlank String link, List<String> tags) {

    public RegisterLinkRequest {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
