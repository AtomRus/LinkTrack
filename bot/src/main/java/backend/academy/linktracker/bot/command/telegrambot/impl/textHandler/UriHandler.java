package backend.academy.linktracker.bot.command.telegrambot.impl.textHandler;

import backend.academy.linktracker.bot.command.annotaion.TextHandler;
import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.telegrambot.AbstractTextHandler;
import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.model.Link;
import backend.academy.linktracker.bot.service.LinkSessionService;
import backend.academy.linktracker.bot.service.TelegramBotService;
import com.pengrad.telegrambot.model.Update;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

// Substate позвовлили нам переиспользовать обработчик URI, иначе пришлось бы создавать новые обработчики URI на каждый
// чих
@Service
@TextHandler(
        description = "Обработчик URI",
        states = {UserState.TRACKING, UserState.UNTRACKING, UserState.REMOVE_TAG, UserState.ADD_TAG})
public class UriHandler extends AbstractTextHandler {
    private final LinkSessionService linkSessionService;

    public UriHandler(
            TelegramBotService telegramBotService,
            Map<String, AbstractRequirement> allRequirements,
            LinkSessionService linkSessionService) {
        super(telegramBotService, allRequirements);
        this.linkSessionService = linkSessionService;
    }

    @Override
    protected void processCommand(Update update) {
        Long chatId = update.message().chat().id();
        Link link = linkSessionService.getSession(chatId);
        link.setLink(URI.create(update.message().text()));
        linkSessionService.updateSession(link);
    }

    @Override
    protected List<String> getRequirementNames() {
        return new ArrayList<>(List.of("textNotEmpty", "textIsUri"));
    }
}
