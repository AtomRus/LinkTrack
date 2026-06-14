package backend.academy.linktracker.loadservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GithubSearchResponse(List<GithubRepoItem> items) {

    public record GithubRepoItem(@JsonProperty("html_url") String htmlUrl) {}
}
