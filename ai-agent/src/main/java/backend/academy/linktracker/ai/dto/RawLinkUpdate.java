package backend.academy.linktracker.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawLinkUpdate(
        @NotNull Long id,
        @NotBlank String description,
        @NotBlank String author,
        @NotEmpty List<Long> tgChatIds) {}
