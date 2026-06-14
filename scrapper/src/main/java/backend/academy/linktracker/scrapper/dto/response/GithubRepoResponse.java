package backend.academy.linktracker.scrapper.dto.response;

import java.time.OffsetDateTime;

public record GithubRepoResponse(OffsetDateTime updated_at, OffsetDateTime pushed_at) {}
