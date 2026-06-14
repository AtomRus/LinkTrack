package backend.academy.linktracker.loadservice.loadtest;

import backend.academy.linktracker.scrapper.grpc.AddLinkRequest;
import backend.academy.linktracker.scrapper.grpc.GetLinksRequest;
import backend.academy.linktracker.scrapper.grpc.LinkServiceGrpc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoadTestService {

    private final LinkServiceGrpc.LinkServiceBlockingStub linkServiceBlockingStub;
    private final ObjectMapper objectMapper;

    public LoadSummaryResponse runBotRegistration(BotRegistrationLoadRequest request) {
        String base = request.botBaseUrl().replaceAll("/+$", "");
        String prefixRaw = request.linkPrefix() == null || request.linkPrefix().isBlank()
                ? "https://loadtest.example/r/"
                : request.linkPrefix();
        final String prefix = prefixRaw.endsWith("/") ? prefixRaw : prefixRaw + "/";
        int workers = request.concurrency() == null || request.concurrency() < 1 ? 32 : request.concurrency();

        RestClient client = RestClient.builder().baseUrl(base).build();
        AtomicInteger ok = new AtomicInteger();
        AtomicInteger dup = new AtomicInteger();
        AtomicInteger err = new AtomicInteger();

        long t0 = System.currentTimeMillis();
        try (ExecutorService pool = Executors.newFixedThreadPool(workers)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < request.iterations(); i++) {
                final int idx = i;
                futures.add(pool.submit(() -> {
                    String url = prefix + idx;
                    try {
                        var res = client.post()
                                .uri("/api/load-test/register-link")
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(objectMapper.writeValueAsString(
                                        new RegisterLinkPayload(request.chatId(), url, List.of())))
                                .retrieve()
                                .toEntity(String.class);
                        if (res.getStatusCode().is2xxSuccessful()) {
                            ok.incrementAndGet();
                        } else if (res.getStatusCode().value() == 409) {
                            dup.incrementAndGet();
                        } else {
                            err.incrementAndGet();
                        }
                    } catch (Exception e) {
                        err.incrementAndGet();
                        log.debug("bot register failed: {}", e.getMessage());
                    }
                }));
            }
            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (Exception e) {
                    err.incrementAndGet();
                }
            }
        }
        long duration = System.currentTimeMillis() - t0;
        double rps = duration == 0 ? 0 : request.iterations() * 1000.0 / duration;
        return new LoadSummaryResponse(duration, request.iterations(), ok.get(), dup.get(), err.get(), rps);
    }

    public LoadSummaryResponse seedScrapper(SeedScrapperRequest request) {
        String prefixRaw = request.linkPrefix() == null || request.linkPrefix().isBlank()
                ? "https://loadseed.example/s/"
                : request.linkPrefix();
        final String prefix = prefixRaw.endsWith("/") ? prefixRaw : prefixRaw + "/";
        int workers = request.concurrency() == null || request.concurrency() < 1 ? 32 : request.concurrency();

        AtomicInteger ok = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();
        long t0 = System.currentTimeMillis();

        try (ExecutorService pool = Executors.newFixedThreadPool(workers)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < request.count(); i++) {
                final int idx = i;
                futures.add(pool.submit(() -> {
                    try {
                        linkServiceBlockingStub.addLink(AddLinkRequest.newBuilder()
                                .setChatId(request.chatId())
                                .setLink(prefix + idx)
                                .build());
                        ok.incrementAndGet();
                    } catch (Exception e) {
                        fail.incrementAndGet();
                        log.debug("seed addLink failed: {}", e.getMessage());
                    }
                }));
            }
            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (Exception e) {
                    fail.incrementAndGet();
                }
            }
        }
        long duration = System.currentTimeMillis() - t0;
        double rps = duration == 0 ? 0 : request.count() * 1000.0 / duration;
        return new LoadSummaryResponse(duration, request.count(), ok.get(), 0, fail.get(), rps);
    }

    public LoadSummaryResponse runListReads(ListReadLoadRequest request) {
        int workers = request.concurrency() == null || request.concurrency() < 1 ? 16 : request.concurrency();
        int warmup = request.warmupIterations() == null ? 100 : Math.max(0, request.warmupIterations());
        GetLinksRequest grpcRequest =
                GetLinksRequest.newBuilder().setChatId(request.chatId()).build();

        for (int i = 0; i < warmup; i++) {
            try {
                linkServiceBlockingStub.getLinks(grpcRequest);
            } catch (Exception ignored) {
                // Прогрев выполняется по best-effort: ошибки на этом этапе не критичны
            }
        }

        AtomicInteger ok = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();
        long t0 = System.currentTimeMillis();
        try (ExecutorService pool = Executors.newFixedThreadPool(workers)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < request.iterations(); i++) {
                futures.add(pool.submit(() -> {
                    try {
                        linkServiceBlockingStub.getLinks(grpcRequest);
                        ok.incrementAndGet();
                    } catch (Exception e) {
                        fail.incrementAndGet();
                        log.debug("getLinks failed: {}", e.getMessage());
                    }
                }));
            }
            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (Exception e) {
                    fail.incrementAndGet();
                }
            }
        }

        long duration = System.currentTimeMillis() - t0;
        double rps = duration == 0 ? 0 : request.iterations() * 1000.0 / duration;
        return new LoadSummaryResponse(duration, request.iterations(), ok.get(), 0, fail.get(), rps);
    }

    public void emitKafkaTestEvent(EmitKafkaUpdateHttpRequest request) {
        String base = request.scrapperBaseUrl().replaceAll("/+$", "");
        RestClient client = RestClient.builder().baseUrl(base).build();
        try {
            client.post()
                    .uri("/api/load-test/emit-link-update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(
                            new EmitBody(request.linkId(), request.url(), request.description(), request.tgChatIds())))
                    .retrieve()
                    .toBodilessEntity();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private record RegisterLinkPayload(Long chatId, String link, List<String> tags) {}

    private record EmitBody(Long linkId, String url, String description, List<Long> tgChatIds) {}
}
