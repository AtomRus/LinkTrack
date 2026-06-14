package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.cache.valkey")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class ValkeyCacheProperties {
    @NotBlank
    private String linksListCacheName = "links-list-cache";

    @NotBlank
    private String linksListByTagCacheName = "links-list-by-tag-cache";

    @NotNull
    private Duration ttl = Duration.ofSeconds(60);

    private boolean nullValuesEnabled = false;

    private boolean clientSideCachingEnabled = false;
}
