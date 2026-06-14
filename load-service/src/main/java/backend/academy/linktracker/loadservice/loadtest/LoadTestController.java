package backend.academy.linktracker.loadservice.loadtest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/load", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class LoadTestController {

    private final LoadTestService loadTestService;

    @PostMapping(path = "/bot-register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoadSummaryResponse botRegister(@Valid @RequestBody BotRegistrationLoadRequest request) {
        return loadTestService.runBotRegistration(request);
    }

    @PostMapping(path = "/seed-scrapper", consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoadSummaryResponse seedScrapper(@Valid @RequestBody SeedScrapperRequest request) {
        return loadTestService.seedScrapper(request);
    }

    @PostMapping(path = "/list-reads", consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoadSummaryResponse listReads(@Valid @RequestBody ListReadLoadRequest request) {
        return loadTestService.runListReads(request);
    }

    @PostMapping(path = "/emit-kafka", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> emitKafka(@Valid @RequestBody EmitKafkaUpdateHttpRequest request) {
        loadTestService.emitKafkaTestEvent(request);
        return ResponseEntity.accepted().build();
    }
}
