package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.GithubClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.KafkaEvent;
import backend.academy.linktracker.scrapper.dto.github.issue.IssuesEventPayload;
import backend.academy.linktracker.scrapper.dto.github.pr.PullRequestEventPayload;
import backend.academy.linktracker.scrapper.dto.response.GithubEventResponse;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowComment;
import backend.academy.linktracker.scrapper.dto.stackoverflow.TrueStackOverflowAnswer;
import backend.academy.linktracker.scrapper.dto.stackoverflow.TrueStackOverflowResponse;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.notification.GithubDescriptions;
import backend.academy.linktracker.scrapper.notification.NotificationSender;
import backend.academy.linktracker.scrapper.metrics.LinksOnTrackMetrics;
import backend.academy.linktracker.scrapper.metrics.OperationMetrics;
import backend.academy.linktracker.scrapper.notification.StackOverflowDescriptions;
import backend.academy.linktracker.scrapper.properties.LinkUpdaterProperties;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkUpdaterScheduler {

    private final GithubClient githubClient;
    private final LinkService linkService;
    private final StackOverflowClient stackOverflowClient;
    private final NotificationSender notificationSender;
    private final TransactionTemplate transactionTemplate;

    @Value("${app.scheduler.await-timeout-ms:60000}")
    private long schedulerAwaitTimeoutMs;

    private final LinkUpdaterProperties linkUpdaterProperties;
    private final OperationMetrics operationMetrics;

    @Scheduled(fixedRateString = "${spring.scheduler.interval}")
    public void update() {
        List<Link> allLinks = linkService.getAssignedLinksToUser();
        log.info("Проверка {} ссылок на обновления", allLinks.size());
        List<CompletableFuture<Void>> futures = Collections.emptyList();
        LinkUpdaterProperties.Mode mode = linkUpdaterProperties.getMode();
        Thread t = Thread.currentThread();
        log.info(
                "Проверка {} ссылок на обновления. mode={}, thread={}, isVirtual={}",
                allLinks.size(),
                mode,
                t.getName(),
                t.isVirtual());

        switch (mode) {
            case SINGLE_THREAD -> runSingleThread(allLinks);
            case OS_THREADS -> runOsThreads(allLinks, linkUpdaterProperties.getOsThreads());
            case VIRTUAL_THREADS -> runVirtualThreads(allLinks);
        }

        try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            futures = allLinks.stream()
                    .map(link -> CompletableFuture.runAsync(
                            () -> {
                                String url = link.getLinkUrl();
                                try {
                                    if (url.contains("github.com")) {
                                        processGithub(link);
                                    } else if (url.contains("stackoverflow.com")) {
                                        processStackOverflow(link);
                                    }
                                } catch (Exception e) {
                                    log.warn(
                                            "Произошла ошибка при обращении к внешним API для {}: {}",
                                            url,
                                            e.getMessage());
                                }
                            },
                            executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
                    .get(schedulerAwaitTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            futures.forEach(future -> future.cancel(true));
            log.error("Превышен таймаут ожидания задач планировщика: {} мс", schedulerAwaitTimeoutMs);
        } catch (Exception e) {
            log.error("Ошибка ожидания задач планировщика: {}", e.getMessage());
        }
        log.info("Цикл проверки завершен.");
    }

    private void runSingleThread(List<Link> allLinks) {
        for (Link link : allLinks) {
            processLinkSafe(link);
        }
    }

    private void runOsThreads(List<Link> allLinks, int threads) {
        try (ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threads)) {
            List<CompletableFuture<Void>> futures = allLinks.stream()
                    .map(link -> CompletableFuture.runAsync(() -> processLinkSafe(link), executor))
                    .toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    private void runVirtualThreads(List<Link> allLinks) {
        try (ExecutorService executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = allLinks.stream()
                    .map(link -> CompletableFuture.runAsync(() -> processLinkSafe(link), executor))
                    .toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    private void processLinkSafe(Link link) {
        String url = link.getLinkUrl();
        try {
            if (url.contains("github.com")) {
                processGithub(link);
            } else if (url.contains("stackoverflow.com")) {
                processStackOverflow(link);
            }
        } catch (Exception e) {
            log.warn("Произошла ошибка при обращении к внешним API для {}: {}", url, e.getMessage());
        }
    }

    private void processGithub(Link link) {
        String[] parts = URI.create(link.getLinkUrl()).getPath().split("/");
        if (parts.length < 3) {
            log.warn("Некорректный GitHub URL: {}", link.getLinkUrl());
            return;
        }
        long scrapeStart = System.nanoTime();
        try {
            var response = githubClient.fetchEvents(parts[1], parts[2], link.getLastEtag());

            if (response.getStatusCode().is3xxRedirection()) {
                return;
            }

            List<GithubEventResponse> events = response.getBody();
            if (events == null || events.isEmpty()) {
                return;
            }

            List<Long> tgChatsId = linkService.findChatsByLink(link.getLinkUrl());
            List<KafkaEvent> newEvents = events.stream()
                    .filter(event -> {
                        if (link.getUpdatedAt() == null) {
                            return true;
                        }
                        return event.createdAt().isAfter(link.getUpdatedAt());
                    })
                    .sorted(Comparator.comparing(GithubEventResponse::createdAt))
                    .map(event -> toKafkaEvent(event, link, tgChatsId))
                    .flatMap(Optional::stream)
                    .toList();

            OffsetDateTime newestProcessedEvent = events.stream()
                    .filter(event ->
                            link.getUpdatedAt() == null || event.createdAt().isAfter(link.getUpdatedAt()))
                    .map(GithubEventResponse::createdAt)
                    .max(OffsetDateTime::compareTo)
                    .orElse(link.getUpdatedAt());

            String newEtag = response.getHeaders().getETag();
            transactionTemplate.executeWithoutResult(status -> {
                newEvents.forEach(notificationSender::send);
                if (newestProcessedEvent != null) {
                    link.setUpdatedAt(newestProcessedEvent);
                    link.setLastCheckTime(newestProcessedEvent);
                }
                link.setLastEtag(newEtag);
                linkService.updateLink(link);
            });
        } catch (Exception e) {
            log.error("Ошибка при проверке ссылки {}: {}", link.getLinkUrl(), e.getMessage());
        } finally {
            operationMetrics.recordScrapeDuration(
                    LinksOnTrackMetrics.resolveTrackedSource(link.getLinkUrl()),
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - scrapeStart));
        }
    }

    private void processStackOverflow(Link link) {
        long scrapeStart = System.nanoTime();
        try {
            processStackOverflowInternal(link);
        } finally {
            operationMetrics.recordScrapeDuration(
                    LinksOnTrackMetrics.resolveTrackedSource(link.getLinkUrl()),
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - scrapeStart));
        }
    }

    private void processStackOverflowInternal(Link link) {
        String[] parts = URI.create(link.getLinkUrl()).getPath().split("/");
        if (parts.length >= 3) {
            Long questionId;
            try {
                questionId = Long.parseLong(parts[2]);
            } catch (NumberFormatException e) {
                log.warn("Некорректный StackOverflow URL: {}", link.getLinkUrl());
                return;
            }

            TrueStackOverflowResponse<TrueStackOverflowAnswer> answersResponse =
                    stackOverflowClient.fetchAnswers(questionId, link.getLastCheckTime());
            var commentsResponse = stackOverflowClient.fetchComments(questionId, link.getLastCheckTime());

            Optional<OffsetDateTime> latestAnswerDate = answersResponse.items().stream()
                    .map(item -> item.lastActivityDate())
                    .max(OffsetDateTime::compareTo);

            Optional<OffsetDateTime> latestCommentDate = commentsResponse.items().stream()
                    .map(item -> item.creationDate())
                    .max(OffsetDateTime::compareTo);

            OffsetDateTime newLastUpdate = Stream.of(latestAnswerDate, latestCommentDate)
                    .flatMap(Optional::stream)
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            List<Long> tgChatsId = linkService.findChatsByLink(link.getLinkUrl());
            if (newLastUpdate != null) {
                List<KafkaEvent> events = Stream.concat(
                                answersResponse.items().stream()
                                        .map(ans -> toStackOverflowAnswerEvent(link, ans, tgChatsId)),
                                commentsResponse.items().stream()
                                        .map(com -> toStackOverflowCommentEvent(link, com, tgChatsId)))
                        .toList();

                transactionTemplate.executeWithoutResult(status -> {
                    events.forEach(notificationSender::send);
                    link.setLastCheckTime(newLastUpdate);
                    linkService.updateLink(link);
                });
            }
        }
    }

    private Optional<KafkaEvent> toKafkaEvent(GithubEventResponse event, Link link, List<Long> tgChatsId) {
        String author = event.actor() != null ? event.actor().login() : "unknown";
        switch (event.type()) {
            case "IssuesEvent" -> {
                if (!(event.payload() instanceof IssuesEventPayload payload)) {
                    return Optional.empty();
                }
                if ("opened".equals(payload.action()) || "reopened".equals(payload.action())) {
                    String description = GithubDescriptions.issueDescription(payload.issue());
                    return Optional.of(
                            new KafkaEvent(link.getLinkId(), link.getLinkUrl(), description, tgChatsId, author));
                }
            }
            case "PullRequestEvent" -> {
                if (!(event.payload() instanceof PullRequestEventPayload payload)) {
                    return Optional.empty();
                }
                if ("opened".equals(payload.action()) || "reopened".equals(payload.action())) {
                    String description = GithubDescriptions.pullRequestDescription(payload.pullRequest());
                    if (description == null) {
                        return Optional.empty();
                    }
                    return Optional.of(
                            new KafkaEvent(link.getLinkId(), link.getLinkUrl(), description, tgChatsId, author));
                }
            }
            default -> {}
        }
        return Optional.empty();
    }

    private KafkaEvent toStackOverflowAnswerEvent(Link link, TrueStackOverflowAnswer answer, List<Long> tgChatIds) {
        String description = StackOverflowDescriptions.answerDescription(answer);
        String author = answer.owner() != null ? answer.owner().displayName() : "unknown";
        return new KafkaEvent(link.getLinkId(), link.getLinkUrl(), description, tgChatIds, author);
    }

    private KafkaEvent toStackOverflowCommentEvent(Link link, StackOverflowComment comment, List<Long> tgChatIds) {
        String description = StackOverflowDescriptions.commentDescription(comment);
        String author = comment.owner() != null ? comment.owner().displayName() : "unknown";
        return new KafkaEvent(link.getLinkId(), link.getLinkUrl(), description, tgChatIds, author);
    }
}
