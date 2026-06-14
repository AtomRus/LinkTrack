package backend.academy.linktracker.scrapper.dto.response;

import java.util.List;

public record ListLinksResponse(List<TrackedLinkResponse> links, Integer size) {}
