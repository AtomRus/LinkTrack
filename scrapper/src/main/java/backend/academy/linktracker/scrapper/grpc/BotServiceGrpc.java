package backend.academy.linktracker.scrapper.grpc;

import backend.academy.linktracker.bot.grpc.LinkUpdate;
import backend.academy.linktracker.bot.grpc.UpdateServiceGrpc;
import backend.academy.linktracker.scrapper.dto.github.issue.IssueDto;
import backend.academy.linktracker.scrapper.dto.github.issue.LabelDto;
import backend.academy.linktracker.scrapper.dto.github.pr.PullRequestDto;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowComment;
import backend.academy.linktracker.scrapper.dto.stackoverflow.TrueStackOverflowAnswer;
import backend.academy.linktracker.scrapper.model.Link;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@GrpcService
public class BotServiceGrpc {
    private final UpdateServiceGrpc.UpdateServiceBlockingStub updateServiceStub;

    public void sendMessage(Long id, URI url, String description, List<Long> tgChatIds) {
        LinkUpdate linkUpdate = LinkUpdate.newBuilder()
                .setId(id)
                .setUrl(url.toString())
                .setDescription(description)
                .addAllTgChatIds(tgChatIds)
                .build();
        try {
            updateServiceStub.sendUpdate(linkUpdate);
        } catch (StatusRuntimeException e) {
            Status.Code code = e.getStatus().getCode();
            if (code == Status.Code.UNAVAILABLE || code == Status.Code.DEADLINE_EXCEEDED) {
                log.warn("Временная ошибка gRPC при отправке обновления {}: {}", id, code, e);
                return;
            }
            log.error("gRPC ошибка при отправке обновления {}: {}", id, code, e);
        } catch (RuntimeException e) {
            log.error("Неожиданная ошибка при отправке обновления {} на url {}:", id, url, e);
        }
    }

    public void sendIssueNotification(Long chatId, URI link, IssueDto issue, List<Long> tgChatIds) {
        String labels = issue.labels().isEmpty()
                ? "нет"
                : String.join(", ", issue.labels().stream().map(LabelDto::name).toList());

        String rawBody = issue.body() != null ? issue.body() : "Описание отсутствует";
        String shortDescription = rawBody.length() > 200 ? rawBody.substring(0, 197) + "..." : rawBody;

        String createdAt = issue.createdAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        // Заменяем \n на %n для соответствия стандартам String.format
        String description = String.format(
                "🎁 *Новый Issue в репозитории* %n" + "📌 *Название:* %s%n"
                        + "🏷 *Метки:* %s%n"
                        + "👤 *Автор:* [%s](https://github.com/%s)%n"
                        + "📅 *Создано:* %s%n%n"
                        + "📝 *Описание:*%n%s%n%n"
                        + "🔗 [Открыть на GitHub](%s)",
                issue.title(),
                labels,
                issue.user().login(),
                issue.user().login(),
                createdAt,
                shortDescription,
                issue.htmlUrl());
        sendMessage(chatId, link, description, tgChatIds);
    }

    public void sendPRNotification(Long chatId, URI link, PullRequestDto pr, List<Long> tgChatIds) {
        String branchFlow =
                String.format("`%s` ➔ `%s`", pr.head().ref(), pr.base().ref());

        String rawBody = pr.body() != null ? pr.body() : "Описание отсутствует";
        String shortDescription = rawBody.length() > 200 ? rawBody.substring(0, 197) + "..." : rawBody;

        OffsetDateTime date = pr.createdAt() != null ? pr.createdAt() : OffsetDateTime.now();
        String createdAt = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        if (pr.user() == null) {
            log.warn("Ignored PullRequest {} because user is null", pr.id());
            return;
        }

        String description = String.format(
                "🚀 *Новый Pull Request в *%n" + "📌 *Название:* %s%n"
                        + "🔄 *Ветки:* %s%n"
                        + "👤 *Автор:* [%s](https://github.com/%s)%n"
                        + "📅 *Создано:* %s%n%n"
                        + "📝 *Описание:*%n%s%n%n"
                        + "🔗 [Проверить PR на GitHub](%s)",
                pr.title(),
                branchFlow,
                pr.user().login(),
                pr.user().login(),
                createdAt,
                shortDescription,
                pr.htmlUrl());
        sendMessage(chatId, link, description, tgChatIds);
    }

    public void sendNewAnswer(Link link, TrueStackOverflowAnswer answer, List<Long> tgChatIds) {
        String cleanBody = answer.body().replaceAll("<[^>]*>", "");
        String shortBody = cleanBody.length() > 200 ? cleanBody.substring(0, 197) + "..." : cleanBody;

        String status = answer.isAccepted() ? "✅ *Принят автором*" : "💡 *Новое решение*";
        String answerUrl = "https://stackoverflow.com/a/" + answer.answerId();

        String description = String.format(
                "🚀 %s%n%n" + "👤 *Автор:* [%s](%s)%n"
                        + "📅 *Дата:* %s%n%n"
                        + "📝 *Текст ответа:*%n_%s_%n%n"
                        + "🔗 [Читать полностью](%s)",
                status,
                answer.owner().displayName(),
                answer.owner().link(),
                answer.creationDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                shortBody,
                answerUrl);
        sendMessage(link.getChatId(), URI.create(link.getLinkUrl()), description, tgChatIds);
    }

    public void sendNewComment(Link link, StackOverflowComment comment, List<Long> tgChatIds) {
        String cleanBody = comment.body().replaceAll("<[^>]*>", "");
        String commentUrl = String.format(
                "https://stackoverflow.com/questions/%d#comment%d_%d",
                comment.postId(), comment.commentId(), comment.postId());

        String description = String.format(
                "💬 *Новый комментарий на Stack Overflow*%n%n" + "❓ *К вопросу:* _%s_%n"
                        + "👤 *От:* [%s](%s)%n%n"
                        + "🗨 *Текст:* %s%n%n"
                        + "🔗 [Открыть на сайте](%s)",
                cleanBody, comment.owner().displayName(), comment.owner().link(), cleanBody, commentUrl);
        sendMessage(link.getChatId(), URI.create(link.getLinkUrl()), description, tgChatIds);
    }
}
