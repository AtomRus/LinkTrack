package backend.academy.linktracker.loadservice.loadtest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record EmitKafkaUpdateHttpRequest(
        @NotBlank String scrapperBaseUrl,
        @NotNull Long linkId,
        @NotBlank String url,
        @NotBlank String description,
        @NotEmpty List<Long> tgChatIds) {}
