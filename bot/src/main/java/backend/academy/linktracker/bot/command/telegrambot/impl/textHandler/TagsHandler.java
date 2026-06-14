package backend.academy.linktracker.bot.command.telegrambot.impl.textHandler;

import backend.academy.linktracker.bot.command.annotaion.TextHandler;
import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.telegrambot.AbstractTextHandler;
import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.model.Link;
import backend.academy.linktracker.bot.service.LinkSessionService;
import backend.academy.linktracker.bot.service.TelegramBotService;
import com.pengrad.telegrambot.model.Update;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
@TextHandler(
        description = "Обработчик тегов",
        states = {UserState.TRACKING})
public class TagsHandler extends AbstractTextHandler {
    private final LinkSessionService linkSessionService;

    public TagsHandler(
            TelegramBotService telegramBotService,
            Map<String, AbstractRequirement> allRequirements,
            LinkSessionService linkSessionService) {
        super(telegramBotService, allRequirements);
        this.linkSessionService = linkSessionService;
    }

    @Override
    protected void processCommand(Update update) {
        List<String> tags = parseTags(update.message().text());
        Link link = linkSessionService.getSession(update.message().chat().id());
        link.setTags(tags);
        linkSessionService.updateSession(link);
    }

    private List<String> parseTags(String input) {
        if (input == null || input.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(input.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    protected List<String> getRequirementNames() {
        return new ArrayList<>(List.of("textNotEmpty", "textIsTags"));
    }
}
