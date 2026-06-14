package backend.academy.linktracker.bot.integrationtest;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import backend.academy.linktracker.scrapper.grpc.AddLinkRequest;
import backend.academy.linktracker.scrapper.grpc.AddTagRequest;
import backend.academy.linktracker.scrapper.grpc.GetLinksRequest;
import backend.academy.linktracker.scrapper.grpc.GetLinksRequestWithTag;
import backend.academy.linktracker.scrapper.grpc.LinkResponse;
import backend.academy.linktracker.scrapper.grpc.LinkServiceGrpc;
import backend.academy.linktracker.scrapper.grpc.ListLinkResponse;
import backend.academy.linktracker.scrapper.grpc.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.grpc.RemoveTagRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@org.testcontainers.junit.jupiter.Testcontainers(disabledWithoutDocker = true)
@Disabled("Requires freshly built link-tracker-scrapper:latest Docker image (docker build -t link-tracker-scrapper:latest -f scrapper/Dockerfile .)")
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class BotScrapperIntegrationTest {

    static Network network = Network.newNetwork();
    private ManagedChannel channel;
    private LinkServiceGrpc.LinkServiceBlockingStub scrapperStub;

    @Container
    static PostgreSQLContainer postgresContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:16"))
            .withDatabaseName("scrapper")
            .withUsername("postgres")
            .withPassword("postgres")
            .withNetwork(network)
            .withNetworkAliases("postgres");

    @Container
    static GenericContainer<?> scrapperContainer = new GenericContainer<>(
                    DockerImageName.parse("link-tracker-scrapper:latest"))
            .withNetwork(network)
            .withNetworkAliases("scrapper")
            .withExposedPorts(8080, 9091)
            .withEnv("DB_URL", "jdbc:postgresql://postgres:5432/scrapper")
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/scrapper")
            .withEnv("SPRING_DATASOURCE_USERNAME", "postgres")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "postgres")
            .dependsOn(postgresContainer)
            .waitingFor(Wait.forLogMessage(".*Started ScrapperApplication.*", 1))
            .withLogConsumer(new Slf4jLogConsumer(log));

    @DynamicPropertySource
    static void grpcProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "app.scrapper-settings.default-address",
                () -> scrapperContainer.getHost() + ":" + scrapperContainer.getMappedPort(9091));
    }

    @BeforeEach
    void setUpChannel() {
        channel = ManagedChannelBuilder.forAddress(scrapperContainer.getHost(), scrapperContainer.getMappedPort(9091))
                .usePlaintext()
                .build();
        scrapperStub = LinkServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDownChannel() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    @Test
    void smokeTest_ContainersAreUpAndCommunicating() {

        AddLinkRequest request = AddLinkRequest.newBuilder()
                .setChatId(777L)
                .setLink("https://github.com/spring-projects/spring-boot")
                .addAllTags(List.of("java", "spring"))
                .build();

        GetLinksRequest getLinksRequest =
                GetLinksRequest.newBuilder().setChatId(777L).build();

        ListLinkResponse listLinkResponse = null;
        try {
            scrapperStub.addLink(request);
            listLinkResponse = scrapperStub.getLinks(getLinksRequest);
            log.info("--- ЗАПРОС УСПЕШНО ОБРАБОТАН ---");
            System.out.println(listLinkResponse.getLinksList().toString());
        } catch (Exception e) {
            fail("Запрос помер");
        } finally {
            channel.shutdown();
        }
    }

    @Test
    void shouldAddLinkWithoutTagsAndRetrieveIt() {
        Long chatId = 101L;
        String url = "https://github.com/test/notags";

        scrapperStub.addLink(
                AddLinkRequest.newBuilder().setChatId(chatId).setLink(url).build());

        ListLinkResponse response = scrapperStub.getLinks(
                GetLinksRequest.newBuilder().setChatId(chatId).build());

        assertThat(response.getLinksList()).extracting(LinkResponse::getLink).contains(url);
    }

    @Test
    void shouldAddLinkWithTagsAndRetrieveByTag() {
        Long chatId = 102L;
        String url = "https://stackoverflow.com/questions/123";
        String tag = "java";

        scrapperStub.addLink(AddLinkRequest.newBuilder()
                .setChatId(chatId)
                .setLink(url)
                .addTags(tag)
                .build());

        ListLinkResponse response = scrapperStub.getLinksByTag(GetLinksRequestWithTag.newBuilder()
                .setChatId(chatId)
                .setTag(tag)
                .build());

        assertThat(response.getLinksList()).extracting(LinkResponse::getLink).contains(url);
    }

    @Test
    void shouldRemoveLink() {
        Long chatId = 103L;
        String url = "https://github.com/toremove";

        scrapperStub.addLink(
                AddLinkRequest.newBuilder().setChatId(chatId).setLink(url).build());

        scrapperStub.removeLink(
                RemoveLinkRequest.newBuilder().setChatId(chatId).setLink(url).build());

        ListLinkResponse response = scrapperStub.getLinks(
                GetLinksRequest.newBuilder().setChatId(chatId).build());

        assertThat(response.getLinksList()).extracting(LinkResponse::getLink).doesNotContain(url);
    }

    @Test
    void shouldAddTagToExistingLink() {
        Long chatId = 104L;
        String url = "https://github.com/addtag";
        String newTag = "spring";

        scrapperStub.addLink(
                AddLinkRequest.newBuilder().setChatId(chatId).setLink(url).build());

        scrapperStub.addTag(AddTagRequest.newBuilder()
                .setChatId(chatId)
                .setLink(url)
                .setTag(newTag)
                .build());

        ListLinkResponse response = scrapperStub.getLinksByTag(GetLinksRequestWithTag.newBuilder()
                .setChatId(chatId)
                .setTag(newTag)
                .build());

        assertThat(response.getLinksList()).extracting(LinkResponse::getLink).contains(url);
    }

    @Test
    void shouldRemoveTagFromLink() {
        Long chatId = 105L;
        String url = "https://github.com/removetag";
        String tagToRemove = "obsolete";

        scrapperStub.addLink(AddLinkRequest.newBuilder()
                .setChatId(chatId)
                .setLink(url)
                .addTags(tagToRemove)
                .build());

        scrapperStub.removeTag(RemoveTagRequest.newBuilder()
                .setChatId(chatId)
                .setLink(url)
                .setTag(tagToRemove)
                .build());

        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> {
            scrapperStub.getLinksByTag(GetLinksRequestWithTag.newBuilder()
                    .setChatId(chatId)
                    .setTag(tagToRemove)
                    .build());
        });

        assertThat(exception.getStatus().getCode()).isEqualTo(io.grpc.Status.Code.NOT_FOUND);
    }
}
