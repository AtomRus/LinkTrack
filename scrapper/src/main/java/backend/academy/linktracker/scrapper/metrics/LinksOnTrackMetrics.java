package backend.academy.linktracker.scrapper.metrics;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LinksOnTrackMetrics {

    private static final String GITHUB = "github";
    private static final String STACKOVERFLOW = "stackoverflow";

    private final LinkRepository linkRepository;
    private final AtomicLong githubCount = new AtomicLong();
    private final AtomicLong stackOverflowCount = new AtomicLong();

    public LinksOnTrackMetrics(LinkRepository linkRepository, MeterRegistry meterRegistry) {
        this.linkRepository = linkRepository;
        Gauge.builder("links_on_track_total", githubCount, AtomicLong::get)
                .description("Количество ссылок на мониторинге")
                .tag("tracked_source", GITHUB)
                .register(meterRegistry);
        Gauge.builder("links_on_track_total", stackOverflowCount, AtomicLong::get)
                .description("Количество ссылок на мониторинге")
                .tag("tracked_source", STACKOVERFLOW)
                .register(meterRegistry);
        refresh();
    }

    @Scheduled(fixedDelayString = "${app.metrics.links-refresh-ms:60000}")
    public void refresh() {
        List<Link> links = linkRepository.findAll();
        long github = links.stream()
                .filter(link -> link.getLinkUrl().contains("github.com"))
                .count();
        long stackoverflow = links.stream()
                .filter(link -> link.getLinkUrl().contains("stackoverflow.com"))
                .count();
        githubCount.set(github);
        stackOverflowCount.set(stackoverflow);
    }

    public static String resolveTrackedSource(String url) {
        if (url.contains("github.com")) {
            return GITHUB;
        }
        if (url.contains("stackoverflow.com")) {
            return STACKOVERFLOW;
        }
        return "other";
    }
}
