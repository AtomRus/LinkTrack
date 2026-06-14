package backend.academy.linktracker.loadservice.api;

import backend.academy.linktracker.loadservice.client.GithubSearchClient;
import backend.academy.linktracker.loadservice.client.StackExchangeSearchClient;
import backend.academy.linktracker.scrapper.grpc.AddLinkRequest;
import backend.academy.linktracker.scrapper.grpc.LinkServiceGrpc;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/load", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class LoadController {

    private final LinkServiceGrpc.LinkServiceBlockingStub linkServiceBlockingStub;
    private final GithubSearchClient githubSearchClient;
    private final StackExchangeSearchClient stackExchangeSearchClient;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoadRunResponse run(@Valid @RequestBody LoadRunRequest request) {
        String githubQ = request.githubQuery();
        if (request.source() == LoadRunRequest.Source.github && (githubQ == null || githubQ.isBlank())) {
            githubQ = "language:java stars:>100";
        }
        List<String> urls =
                switch (request.source()) {
                    case github -> githubSearchClient.fetchRepositoryUrls(githubQ, request.count());
                    case stackoverflow -> stackExchangeSearchClient.fetchQuestionUrls(request.count());
                };

        int ok = 0;
        int fail = 0;
        for (String url : urls) {
            try {
                linkServiceBlockingStub.addLink(AddLinkRequest.newBuilder()
                        .setChatId(request.chatId())
                        .setLink(url)
                        .build());
                ok++;
            } catch (Exception e) {
                fail++;
                log.warn("addLink failed for {}: {}", url, e.getMessage());
            }
        }
        return new LoadRunResponse(ok, fail, urls.size());
    }
}
