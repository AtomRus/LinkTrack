package backend.academy.linktracker.loadservice.api;

public record LoadRunResponse(int succeeded, int failed, int fetched) {}
