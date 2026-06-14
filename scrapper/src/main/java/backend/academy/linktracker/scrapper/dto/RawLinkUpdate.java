package backend.academy.linktracker.scrapper.dto;

import java.util.List;

public record RawLinkUpdate(long id, String description, String author, List<Long> tgChatIds) {}
