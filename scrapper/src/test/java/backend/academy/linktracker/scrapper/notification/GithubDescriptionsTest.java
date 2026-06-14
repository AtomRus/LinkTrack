package backend.academy.linktracker.scrapper.notification;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.dto.github.UserDto;
import backend.academy.linktracker.scrapper.dto.github.issue.IssueDto;
import backend.academy.linktracker.scrapper.dto.github.issue.LabelDto;
import backend.academy.linktracker.scrapper.dto.github.pr.PullRequestBranchDto;
import backend.academy.linktracker.scrapper.dto.github.pr.PullRequestDto;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class GithubDescriptionsTest {

    @Test
    void issueDescriptionShouldContainTitleAndAuthor() {
        IssueDto issue = new IssueDto(
                1L,
                10,
                "Fix bug",
                "https://github.com/o/r/issues/1",
                new UserDto("dev", null, null),
                "Body text",
                "open",
                List.of(new LabelDto(1L, "bug", "ff0000", "bug")),
                OffsetDateTime.parse("2024-01-15T10:00:00Z"),
                null,
                null);

        String description = GithubDescriptions.issueDescription(issue);

        assertThat(description).contains("Fix bug").contains("dev").contains("bug");
    }

    @Test
    void pullRequestDescriptionShouldReturnNullWithoutUser() {
        PullRequestDto pr = new PullRequestDto(
                2L,
                "Feature",
                "https://github.com/o/r/pull/2",
                null,
                "details",
                "open",
                OffsetDateTime.parse("2024-02-01T12:00:00Z"),
                null,
                new PullRequestBranchDto("feature", "feature"),
                new PullRequestBranchDto("main", "main"));

        assertThat(GithubDescriptions.pullRequestDescription(pr)).isNull();
    }

    @Test
    void pullRequestDescriptionShouldFormatBranches() {
        PullRequestDto pr = new PullRequestDto(
                3L,
                "Feature",
                "https://github.com/o/r/pull/3",
                new UserDto("dev", null, null),
                "details",
                "open",
                OffsetDateTime.parse("2024-02-01T12:00:00Z"),
                null,
                new PullRequestBranchDto("feature", "feature"),
                new PullRequestBranchDto("main", "main"));

        String description = GithubDescriptions.pullRequestDescription(pr);

        assertThat(description).contains("Feature").contains("feature").contains("main");
    }
}
