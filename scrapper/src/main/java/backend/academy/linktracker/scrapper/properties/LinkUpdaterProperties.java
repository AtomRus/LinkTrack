package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.link-updater")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class LinkUpdaterProperties {

    @NotNull
    private Mode mode = Mode.VIRTUAL_THREADS;

    @Min(1)
    private int osThreads = Runtime.getRuntime().availableProcessors();

    public enum Mode {
        SINGLE_THREAD,
        OS_THREADS,
        VIRTUAL_THREADS
    }
}
