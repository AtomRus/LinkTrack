package backend.academy.linktracker.loadservice.client;

import backend.academy.linktracker.loadservice.dto.GithubSearchResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class GithubSearchClient {

    private final RestClient githubRestClient;

    public List<String> fetchRepositoryUrls(String query, int total) {
        List<String> urls = new ArrayList<>();
        int page = 1;
        while (urls.size() < total) {
            final int perPage = Math.min(100, total - urls.size());
            final int pageNo = page;
            GithubSearchResponse response = githubRestClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/repositories")
                            .queryParam("q", query)
                            .queryParam("per_page", perPage)
                            .queryParam("page", pageNo)
                            .build())
                    .retrieve()
                    .body(GithubSearchResponse.class);
            if (response == null || response.items() == null || response.items().isEmpty()) {
                break;
            }
            response.items().forEach(item -> urls.add(item.htmlUrl()));
            if (response.items().size() < perPage) {
                break;
            }
            page++;
        }
        return urls;
    }
}
