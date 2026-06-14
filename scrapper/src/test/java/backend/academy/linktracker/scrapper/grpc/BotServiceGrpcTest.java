package backend.academy.linktracker.scrapper.grpc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.grpc.UpdateServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import backend.academy.linktracker.scrapper.dto.github.UserDto;
import backend.academy.linktracker.scrapper.dto.github.issue.IssueDto;
import backend.academy.linktracker.scrapper.dto.github.issue.LabelDto;
import backend.academy.linktracker.scrapper.dto.github.pr.PullRequestBranchDto;
import backend.academy.linktracker.scrapper.dto.github.pr.PullRequestDto;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowComment;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowUser;
import backend.academy.linktracker.scrapper.dto.stackoverflow.TrueStackOverflowAnswer;
import backend.academy.linktracker.scrapper.model.Link;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BotServiceGrpcTest {

    @Mock
    private UpdateServiceGrpc.UpdateServiceBlockingStub updateServiceStub;

    @InjectMocks
    private BotServiceGrpc botServiceGrpc;

    @Test
    void sendMessageShouldCallGrpcStubOnSuccess() {
        botServiceGrpc.sendMessage(1L, URI.create("https://example.com"), "description", List.of(1L, 2L));

        verify(updateServiceStub).sendUpdate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void sendMessageShouldNotThrowOnTransientGrpcError() {
        doThrow(new StatusRuntimeException(Status.UNAVAILABLE))
                .when(updateServiceStub)
                .sendUpdate(org.mockito.ArgumentMatchers.any());

        botServiceGrpc.sendMessage(2L, URI.create("https://example.com"), "description", List.of(1L));

        verify(updateServiceStub).sendUpdate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void sendMessageShouldNotThrowOnUnexpectedError() {
        doThrow(new IllegalStateException("boom"))
                .when(updateServiceStub)
                .sendUpdate(org.mockito.ArgumentMatchers.any());

        botServiceGrpc.sendMessage(3L, URI.create("https://example.com"), "description", List.of(1L));

        verify(updateServiceStub).sendUpdate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void sendIssueNotificationShouldBuildFormattedMessage() {
        IssueDto issue = new IssueDto(
                1L,
                10,
                "Bug",
                "https://github.com/o/r/issues/1",
                new UserDto("dev", null, null),
                "Body",
                "open",
                List.of(new LabelDto(1L, "bug", "f00", "bug")),
                OffsetDateTime.parse("2024-01-01T10:00:00Z"),
                null,
                null);

        botServiceGrpc.sendIssueNotification(1L, URI.create("https://github.com/o/r"), issue, List.of(10L));

        verify(updateServiceStub).sendUpdate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void sendPrNotificationShouldSkipWhenUserMissing() {
        PullRequestDto pr = new PullRequestDto(
                2L,
                "PR",
                "https://github.com/o/r/pull/2",
                null,
                "body",
                "open",
                OffsetDateTime.parse("2024-01-01T10:00:00Z"),
                null,
                new PullRequestBranchDto("f", "feature"),
                new PullRequestBranchDto("m", "main"));

        botServiceGrpc.sendPRNotification(1L, URI.create("https://github.com/o/r"), pr, List.of(10L));

        verify(updateServiceStub, org.mockito.Mockito.never()).sendUpdate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void sendNewAnswerShouldDelegateToSendMessage() {
        Link link = new Link(1L, 2L, "https://stackoverflow.com/q/1", null, null, null, List.of());
        TrueStackOverflowAnswer answer = new TrueStackOverflowAnswer(
                99L,
                false,
                OffsetDateTime.parse("2024-01-01T10:00:00Z"),
                OffsetDateTime.parse("2024-01-01T10:00:00Z"),
                "answer body",
                new StackOverflowUser("dev", 1L, "https://stackoverflow.com/users/1/dev"));

        botServiceGrpc.sendNewAnswer(link, answer, List.of(10L));

        verify(updateServiceStub).sendUpdate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void sendNewCommentShouldDelegateToSendMessage() {
        Link link = new Link(1L, 2L, "https://stackoverflow.com/q/1", null, null, null, List.of());
        StackOverflowComment comment = new StackOverflowComment(
                7L,
                123L,
                OffsetDateTime.parse("2024-01-01T10:00:00Z"),
                "comment",
                new StackOverflowUser("dev", 1L, "https://stackoverflow.com/users/1/dev"));

        botServiceGrpc.sendNewComment(link, comment, List.of(10L));

        verify(updateServiceStub).sendUpdate(org.mockito.ArgumentMatchers.any());
    }
}
