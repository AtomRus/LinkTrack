package backend.academy.linktracker.scrapper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.GithubClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.github.UserDto;
import backend.academy.linktracker.scrapper.dto.github.issue.IssueDto;
import backend.academy.linktracker.scrapper.dto.github.issue.IssuesEventPayload;
import backend.academy.linktracker.scrapper.dto.github.issue.LabelDto;
import backend.academy.linktracker.scrapper.dto.response.GithubEventResponse;
import backend.academy.linktracker.scrapper.dto.stackoverflow.TrueStackOverflowResponse;
import backend.academy.linktracker.scrapper.metrics.OperationMetrics;
import backend.academy.linktracker.scrapper.notification.NotificationSender;
import backend.academy.linktracker.scrapper.properties.LinkUpdaterProperties;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class LinkUpdaterSchedulerTest {

    @Mock
    private GithubClient githubClient;

    @Mock
    private LinkService linkService;

    @Mock
    private StackOverflowClient stackOverflowClient;

    @Mock
    private NotificationSender notificationSender;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private LinkUpdaterProperties linkUpdaterProperties;

    @Mock
    private OperationMetrics operationMetrics;

    @InjectMocks
    private LinkUpdaterScheduler scheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "schedulerAwaitTimeoutMs", 5000L);
        when(linkUpdaterProperties.getMode()).thenReturn(LinkUpdaterProperties.Mode.VIRTUAL_THREADS);
        org.mockito.Mockito.lenient()
                .when(operationMetrics.record(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(java.util.function.Supplier.class)))
                .thenAnswer(inv -> inv.getArgument(2, java.util.function.Supplier.class).get());
        org.mockito.Mockito.lenient()
                .doAnswer(inv -> {
                    inv.getArgument(2, Runnable.class).run();
                    return null;
                })
                .when(operationMetrics)
                .record(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(Runnable.class));
    }

    @Test
    void updateShouldFinishWhenNoLinks() {
        when(linkService.getAssignedLinksToUser()).thenReturn(Collections.emptyList());

        scheduler.update();

        verify(githubClient, never()).fetchEvents(anyString(), anyString(), nullable(String.class));
        verify(stackOverflowClient, never()).fetchAnswers(anyLong(), any());
    }

    @Test
    void updateShouldSkipUnknownHosts() {
        backend.academy.linktracker.scrapper.model.Link link =
                new backend.academy.linktracker.scrapper.model.Link(1L, 2L, "https://example.com", null, null, null, List.of());
        when(linkService.getAssignedLinksToUser()).thenReturn(List.of(link));

        scheduler.update();

        verify(githubClient, never()).fetchEvents(anyString(), anyString(), nullable(String.class));
    }

    @Test
    void updateShouldQueryStackOverflowForQuestionUrl() {
        backend.academy.linktracker.scrapper.model.Link link = new backend.academy.linktracker.scrapper.model.Link(
                1L, 2L, "https://stackoverflow.com/questions/12345/title", null, null, null, List.of());
        when(linkService.getAssignedLinksToUser()).thenReturn(List.of(link));
        when(stackOverflowClient.fetchAnswers(12345L, null))
                .thenReturn(new TrueStackOverflowResponse<>(List.of(), false, 100, 99));
        when(stackOverflowClient.fetchComments(12345L, null))
                .thenReturn(new TrueStackOverflowResponse<>(List.of(), false, 100, 99));

        scheduler.update();

        verify(stackOverflowClient, atLeastOnce()).fetchAnswers(12345L, null);
        verify(stackOverflowClient, atLeastOnce()).fetchComments(12345L, null);
    }

    @Test
    void updateShouldPublishGithubIssueEvents() {
        backend.academy.linktracker.scrapper.model.Link link = new backend.academy.linktracker.scrapper.model.Link(
                1L, 2L, "https://github.com/owner/repo", null, null, null, List.of());
        when(linkService.getAssignedLinksToUser()).thenReturn(List.of(link));

        IssueDto issue = new IssueDto(
                1L,
                1,
                "Bug",
                "https://github.com/owner/repo/issues/1",
                new UserDto("dev", null, null),
                "Body",
                "open",
                List.of(new LabelDto(1L, "bug", "f00", "bug")),
                OffsetDateTime.parse("2024-06-01T10:00:00Z"),
                null,
                null);
        IssuesEventPayload payload = new IssuesEventPayload("opened", issue);
        GithubEventResponse event = new GithubEventResponse("1", "IssuesEvent", new UserDto("dev", null, null), payload, OffsetDateTime.parse("2024-06-01T10:00:00Z"));

        when(githubClient.fetchEvents("owner", "repo", null))
                .thenReturn(ResponseEntity.ok().eTag("etag-v2").body(List.of(event)));
        when(linkService.findChatsByLink(link.getLinkUrl())).thenReturn(List.of(100L));
        org.mockito.Mockito.doAnswer(inv -> {
                    java.util.function.Consumer<org.springframework.transaction.TransactionStatus> callback =
                            inv.getArgument(0);
                    callback.accept(null);
                    return null;
                })
                .when(transactionTemplate)
                .executeWithoutResult(any());

        scheduler.update();

        ArgumentCaptor<backend.academy.linktracker.scrapper.dto.KafkaEvent> captor =
                ArgumentCaptor.forClass(backend.academy.linktracker.scrapper.dto.KafkaEvent.class);
        verify(notificationSender, org.mockito.Mockito.atLeastOnce()).send(captor.capture());
        verify(linkService, org.mockito.Mockito.atLeastOnce()).updateLink(link);
    }
}
