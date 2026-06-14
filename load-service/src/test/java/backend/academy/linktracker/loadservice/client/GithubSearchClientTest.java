package backend.academy.linktracker.loadservice.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

class GithubSearchClientTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void fetchRepositoryUrlsShouldReturnHtmlUrls() {
        wireMockServer.stubFor(get(urlPathEqualTo("/search/repositories"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                """
                                {"items":[{"html_url":"https://github.com/a/b"}]}
                                """)));

        GithubSearchClient client = new GithubSearchClient(restClient());
        assertThat(client.fetchRepositoryUrls("java", 1)).containsExactly("https://github.com/a/b");
    }

    private RestClient restClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(wireMockServer.baseUrl())
                .build();
    }
}
