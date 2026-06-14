package backend.academy.linktracker.scrapper.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinksOnTrackMetricsTest {

    @Mock
    private LinkRepository linkRepository;

    @Test
    void shouldRegisterGaugesByTrackedSource() {
        when(linkRepository.findAll())
                .thenReturn(List.of(
                        new Link(1L, 1L, "https://github.com/a/b", null, null, null, List.of()),
                        new Link(1L, 2L, "https://stackoverflow.com/q/1", null, null, null, List.of()),
                        new Link(1L, 3L, "https://example.com/x", null, null, null, List.of())));

        MeterRegistry registry = new SimpleMeterRegistry();
        new LinksOnTrackMetrics(linkRepository, registry);

        Gauge github = registry.find("links_on_track_total").tag("tracked_source", "github").gauge();
        Gauge stackoverflow =
                registry.find("links_on_track_total").tag("tracked_source", "stackoverflow").gauge();

        assertThat(github).isNotNull();
        assertThat(stackoverflow).isNotNull();
        assertThat(github.value()).isEqualTo(1.0);
        assertThat(stackoverflow.value()).isEqualTo(1.0);
        assertThat(LinksOnTrackMetrics.resolveTrackedSource("https://github.com/x")).isEqualTo("github");
        assertThat(LinksOnTrackMetrics.resolveTrackedSource("https://stackoverflow.com/q/1"))
                .isEqualTo("stackoverflow");
    }
}
