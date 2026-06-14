package backend.academy.linktracker.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record TrueStackOverflowAnswer(
        @JsonProperty("answer_id") long answerId,
        @JsonProperty("is_accepted") boolean isAccepted,

        @JsonProperty("creation_date") @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
        OffsetDateTime creationDate,

        @JsonProperty("last_activity_date") @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
        OffsetDateTime lastActivityDate,

        String body,
        StackOverflowUser owner) {}
