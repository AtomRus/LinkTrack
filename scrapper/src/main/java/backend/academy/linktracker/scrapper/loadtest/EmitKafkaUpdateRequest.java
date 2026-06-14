package backend.academy.linktracker.scrapper.loadtest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record EmitKafkaUpdateRequest(
        @NotNull Long linkId,
        @NotBlank String url,
        @NotBlank String description,
        @NotEmpty List<Long> tgChatIds) {}
