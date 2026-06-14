package backend.academy.linktracker.scrapper.integrationtest;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractIntegrationTest {

    @org.testcontainers.junit.jupiter.Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("scrapper")
            .withUsername("user")
            .withPassword("password");

    @org.testcontainers.junit.jupiter.Container
    static final GenericContainer<?> VALKEY = new GenericContainer<>("valkey/valkey:8.0").withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", VALKEY::getHost);
        registry.add("spring.data.redis.port", () -> VALKEY.getMappedPort(6379));
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("app.scheduler.enabled", () -> "false");
        registry.add("app.database-access-type", () -> "jpa");
        registry.add("app.cache.valkey.ttl", () -> "120s");
        registry.add("spring.cache.type", () -> "redis");
    }
}
