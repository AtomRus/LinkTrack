package backend.academy.linktracker.bot.command.telegrambot.impl.command;

import backend.academy.linktracker.bot.command.annotaion.Command;
import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.telegrambot.AbstractTelegramCommandHandler;
import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.service.LinkSessionService;
import backend.academy.linktracker.bot.service.TelegramBotService;
import backend.academy.linktracker.bot.service.UserStateService;
import com.pengrad.telegrambot.model.Update;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
@Command(
        command = "/cancel",
        description = "Отменить регистрацию ссылки",
        states = {UserState.TRACKING, UserState.UNTRACKING, UserState.ADD_TAG, UserState.REMOVE_TAG})
public class CancelCommandHandler extends AbstractTelegramCommandHandler {
    private final LinkSessionService linkSessionService;
    private final UserStateService userStateService;
    private final TelegramBotService telegramBotService;

    public CancelCommandHandler(
            TelegramBotService telegramBotService,
            Map<String, AbstractRequirement> allRequirements,
            LinkSessionService linkSessionService,
            UserStateService userStateService,
            TelegramBotService telegramBotService1) {
        super(telegramBotService, allRequirements);
        this.linkSessionService = linkSessionService;
        this.userStateService = userStateService;
        this.telegramBotService = telegramBotService1;
    }

    @Override
    protected void processCommand(Update update) {
        Long chatId = update.message().chat().id();
        linkSessionService.deleteDraft(chatId);
        telegramBotService.sendMessage(chatId, "Возврат в меню");
        userStateService.updateUserState(chatId, UserState.MENU);
        userStateService.updateUserSubstate(chatId, UserState.SubState.START);
    }
}
