package backend.academy.linktracker.bot.command.telegrambot.impl.command;

import backend.academy.linktracker.bot.command.annotaion.Command;
import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.telegrambot.AbstractTelegramCommandHandler;
import backend.academy.linktracker.bot.command.telegrambot.CommandNameEnum;
import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.model.Link;
import backend.academy.linktracker.bot.service.LinkSessionService;
import backend.academy.linktracker.bot.service.TelegramBotService;
import backend.academy.linktracker.bot.service.UserStateService;
import com.pengrad.telegrambot.model.Update;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
@Command(
        command = "/addTag",
        description = "Добавить тег ссылке",
        states = {UserState.MENU})
public class AddTagCommandHandler extends AbstractTelegramCommandHandler {
    private final UserStateService userStateService;
    private final LinkSessionService linkSessionService;

    public AddTagCommandHandler(
            TelegramBotService telegramBotService,
            Map<String, AbstractRequirement> allRequirements,
            UserStateService userStateService,
            LinkSessionService linkSessionService) {
        super(telegramBotService, allRequirements);
        this.userStateService = userStateService;
        this.linkSessionService = linkSessionService;
    }

    @Override
    protected void processCommand(Update update) {
        Long chatId = update.message().chat().id();
        linkSessionService.updateSession(new Link(chatId, null, null));
        userStateService.updateUserState(chatId, UserState.ADD_TAG);
        userStateService.updateUserSubstate(chatId, UserState.SubState.START);
    }

    @Override
    public String getHandlerName() {
        return CommandNameEnum.ADD_TAG.getName();
    }
}
