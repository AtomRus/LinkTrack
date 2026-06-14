package backend.academy.linktracker.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record StackOverflowComment(
        @JsonProperty("comment_id") long commentId,
        @JsonProperty("post_id") long postId,

        @JsonProperty("creation_date") @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
        OffsetDateTime creationDate,

        String body,
        StackOverflowUser owner) {}
