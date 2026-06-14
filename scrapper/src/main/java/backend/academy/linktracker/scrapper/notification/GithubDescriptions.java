package backend.academy.linktracker.scrapper.notification;

import backend.academy.linktracker.scrapper.dto.github.issue.IssueDto;
import backend.academy.linktracker.scrapper.dto.github.issue.LabelDto;
import backend.academy.linktracker.scrapper.dto.github.pr.PullRequestDto;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public final class GithubDescriptions {
    private GithubDescriptions() {}

    public static String issueDescription(IssueDto issue) {
        String labels = issue.labels().isEmpty()
                ? "нет"
                : String.join(", ", issue.labels().stream().map(LabelDto::name).toList());

        String rawBody = issue.body() != null ? issue.body() : "Описание отсутствует";
        String shortDescription = rawBody.length() > 200 ? rawBody.substring(0, 197) + "..." : rawBody;

        String createdAt = issue.createdAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        return String.format(
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
    }

    public static String pullRequestDescription(PullRequestDto pr) {
        String branchFlow =
                String.format("`%s` ➔ `%s`", pr.head().ref(), pr.base().ref());

        String rawBody = pr.body() != null ? pr.body() : "Описание отсутствует";
        String shortDescription = rawBody.length() > 200 ? rawBody.substring(0, 197) + "..." : rawBody;

        OffsetDateTime date = pr.createdAt() != null ? pr.createdAt() : OffsetDateTime.now();
        String createdAt = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        if (pr.user() == null) {
            return null;
        }

        return String.format(
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
    }
}
