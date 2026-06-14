package backend.academy.linktracker.scrapper.notification;

import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowComment;
import backend.academy.linktracker.scrapper.dto.stackoverflow.TrueStackOverflowAnswer;
import java.time.format.DateTimeFormatter;

public final class StackOverflowDescriptions {
    private StackOverflowDescriptions() {}

    public static String answerDescription(TrueStackOverflowAnswer answer) {
        String cleanBody = answer.body().replaceAll("<[^>]*>", "");
        String shortBody = cleanBody.length() > 200 ? cleanBody.substring(0, 197) + "..." : cleanBody;

        String status = answer.isAccepted() ? "✅ *Принят автором*" : "💡 *Новое решение*";
        String answerUrl = "https://stackoverflow.com/a/" + answer.answerId();

        return String.format(
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
    }

    public static String commentDescription(StackOverflowComment comment) {
        String cleanBody = comment.body().replaceAll("<[^>]*>", "");
        String commentUrl = String.format(
                "https://stackoverflow.com/questions/%d#comment%d_%d",
                comment.postId(), comment.commentId(), comment.postId());

        return String.format(
                "💬 *Новый комментарий на Stack Overflow*%n%n" + "❓ *К вопросу:* _%s_%n"
                        + "👤 *От:* [%s](%s)%n%n"
                        + "🗨 *Текст:* %s%n%n"
                        + "🔗 [Открыть на сайте](%s)",
                cleanBody, comment.owner().displayName(), comment.owner().link(), cleanBody, commentUrl);
    }
}
