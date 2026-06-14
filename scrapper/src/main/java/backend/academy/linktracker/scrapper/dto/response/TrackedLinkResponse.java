package backend.academy.linktracker.scrapper.dto.response;

import java.util.List;

public record TrackedLinkResponse(Long id, String url, List<String> tags, List<String> filters) {}
