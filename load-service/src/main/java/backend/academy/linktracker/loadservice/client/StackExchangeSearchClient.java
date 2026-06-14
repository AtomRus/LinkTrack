package backend.academy.linktracker.loadservice.client;

import backend.academy.linktracker.loadservice.dto.StackSearchResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class StackExchangeSearchClient {

    private final RestClient stackExchangeRestClient;

    @Value("${STACKOVERFLOW_KEY:}")
    private String stackOverflowKey;

    public List<String> fetchQuestionUrls(int total) {
        List<String> urls = new ArrayList<>();
        int page = 1;
        while (urls.size() < total) {
            final int pageSize = Math.min(100, total - urls.size());
            final int pageNo = page;
            StackSearchResponse response = stackExchangeRestClient
                    .get()
                    .uri(uriBuilder -> {
                        var b = uriBuilder
                                .path("/search/advanced")
                                .queryParam("order", "desc")
                                .queryParam("sort", "activity")
                                .queryParam("site", "stackoverflow")
                                .queryParam("pagesize", pageSize)
                                .queryParam("page", pageNo);
                        if (stackOverflowKey != null && !stackOverflowKey.isBlank()) {
                            b.queryParam("key", stackOverflowKey);
                        }
                        return b.build();
                    })
                    .retrieve()
                    .body(StackSearchResponse.class);
            if (response == null || response.items() == null || response.items().isEmpty()) {
                break;
            }
            response.items().forEach(item -> urls.add(item.toQuestionUrl()));
            if (response.items().size() < pageSize) {
                break;
            }
            page++;
        }
        return urls;
    }
}
