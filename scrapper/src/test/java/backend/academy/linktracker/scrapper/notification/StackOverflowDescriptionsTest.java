package backend.academy.linktracker.scrapper.notification;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowComment;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowUser;
import backend.academy.linktracker.scrapper.dto.stackoverflow.TrueStackOverflowAnswer;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class StackOverflowDescriptionsTest {

    @Test
    void answerDescriptionShouldStripHtmlAndMarkAccepted() {
        TrueStackOverflowAnswer answer = new TrueStackOverflowAnswer(
                42L,
                true,
                OffsetDateTime.parse("2024-03-01T10:00:00Z"),
                OffsetDateTime.parse("2024-03-01T10:00:00Z"),
                "<p>Hello world</p>",
                new StackOverflowUser("dev", 1L, "https://stackoverflow.com/users/1/dev"));

        String description = StackOverflowDescriptions.answerDescription(answer);

        assertThat(description).contains("Принят автором").contains("dev").contains("Hello world");
    }

    @Test
    void commentDescriptionShouldContainAuthorAndLink() {
        StackOverflowComment comment = new StackOverflowComment(
                7L,
                123L,
                OffsetDateTime.parse("2024-03-02T12:00:00Z"),
                "Nice question",
                new StackOverflowUser("reader", 2L, "https://stackoverflow.com/users/2/reader"));

        String description = StackOverflowDescriptions.commentDescription(comment);

        assertThat(description).contains("Nice question").contains("reader").contains("comment7_123");
    }
}
