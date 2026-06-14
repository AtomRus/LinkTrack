package backend.academy.linktracker.loadservice.loadtest;

public record LoadSummaryResponse(
        long totalDurationMs,
        int iterations,
        int successCount,
        int duplicateCount,
        int errorCount,
        double requestsPerSecond) {}
