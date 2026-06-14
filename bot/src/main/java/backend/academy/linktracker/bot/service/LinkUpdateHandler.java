package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.dto.LinkUpdateEvent;
import backend.academy.linktracker.bot.model.LinkUpdateModel;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkUpdateHandler {
    private final TelegramBotService telegramBotService;

    public void handleEvent(LinkUpdateEvent event) {
        LinkUpdateModel model = new LinkUpdateModel();
        model.setId(event.id());
        model.setUrl(event.url() != null ? URI.create(event.url()) : null);
        model.setDescription(event.description());
        model.setTgChatIds(event.tgChatIds());
        telegramBotService.notifyUsers(model);
    }

    public void handleModel(LinkUpdateModel model) {
        telegramBotService.notifyUsers(model);
    }
}
