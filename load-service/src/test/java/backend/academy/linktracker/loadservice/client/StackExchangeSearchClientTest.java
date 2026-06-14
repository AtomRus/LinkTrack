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

class StackExchangeSearchClientTest {

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
    void fetchQuestionUrlsShouldMapItemsToUrls() {
        wireMockServer.stubFor(get(urlPathEqualTo("/search/advanced"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                """
                                {"items":[{"question_id":12345}]}
                                """)));

        StackExchangeSearchClient client = new StackExchangeSearchClient(restClient());
        ReflectionTestUtils.setField(client, "stackOverflowKey", "");

        assertThat(client.fetchQuestionUrls(1)).containsExactly("https://stackoverflow.com/questions/12345");
    }

    private RestClient restClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(wireMockServer.baseUrl())
                .build();
    }
}
