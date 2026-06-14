package backend.academy.linktracker.bot.loadtest;

import backend.academy.linktracker.bot.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.bot.exception.ResourceNotFoundException;
import backend.academy.linktracker.bot.grpc.ScrapperGrpcService;
import backend.academy.linktracker.bot.model.Link;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/load-test")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.load-test", name = "api-enabled", havingValue = "true")
public class BotLoadTestController {

    private final ScrapperGrpcService scrapperGrpcService;
    private final MeterRegistry meterRegistry;

    @PostMapping("/register-link")
    public ResponseEntity<RegisterLinkResponse> registerLink(@Valid @RequestBody RegisterLinkRequest request) {
        long t0 = System.nanoTime();
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Link link = new Link(null, URI.create(request.link()), request.tags());
            scrapperGrpcService.addLink(request.chatId(), link);
            return ResponseEntity.ok(new RegisterLinkResponse(elapsedMs(t0), "ok"));
        } catch (LinkAlreadyTrackedException e) {
            return ResponseEntity.status(409).body(new RegisterLinkResponse(elapsedMs(t0), "duplicate"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.internalServerError().body(new RegisterLinkResponse(elapsedMs(t0), "error"));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(new RegisterLinkResponse(elapsedMs(t0), "error"));
        } finally {
            sample.stop(Timer.builder("bot.load.http.register")
                    .description("HTTP load-test: register link via bot to scrapper")
                    .publishPercentileHistogram()
                    .register(meterRegistry));
        }
    }

    private static long elapsedMs(long t0Nano) {
        return (System.nanoTime() - t0Nano) / 1_000_000L;
    }
}
