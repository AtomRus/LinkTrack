package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.cache.ClientSideLinksCache;
import backend.academy.linktracker.scrapper.dto.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.dto.response.TrackedLinkResponse;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.service.LinkService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class LinkListController {
    private final LinkService linkService;
    private final ClientSideLinksCache clientSideLinksCache;

    public LinkListController(LinkService linkService, ClientSideLinksCache clientSideLinksCache) {
        this.linkService = linkService;
        this.clientSideLinksCache = clientSideLinksCache;
    }

    @GetMapping("/list")
    public ListLinksResponse getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        List<Link> links = clientSideLinksCache.get(chatId).orElseGet(() -> {
            List<Link> resolved = linkService.getListOfLinks(chatId);
            clientSideLinksCache.put(chatId, resolved);
            return resolved;
        });
        List<TrackedLinkResponse> responseLinks = links.stream()
                .map(link -> new TrackedLinkResponse(link.getLinkId(), link.getLinkUrl(), link.getAllTags(), List.of()))
                .toList();
        return new ListLinksResponse(responseLinks, responseLinks.size());
    }
}
