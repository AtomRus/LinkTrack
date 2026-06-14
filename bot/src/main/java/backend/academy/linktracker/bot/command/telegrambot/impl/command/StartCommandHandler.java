package backend.academy.linktracker.bot.command.telegrambot.impl.command;

import backend.academy.linktracker.bot.command.annotaion.Command;
import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.telegrambot.AbstractTelegramCommandHandler;
import backend.academy.linktracker.bot.command.telegrambot.CommandNameEnum;
import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.service.TelegramBotService;
import backend.academy.linktracker.bot.service.UserStateService;
import com.pengrad.telegrambot.model.Update;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
@Command(
        command = "/start",
        description = "Начать работу с ботом",
        states = {UserState.MENU})
public class StartCommandHandler extends AbstractTelegramCommandHandler {

    private final UserStateService userStateService;

    public StartCommandHandler(
            TelegramBotService telegramBotService,
            Map<String, AbstractRequirement> allRequirements,
            UserStateService userStateService) {
        super(telegramBotService, allRequirements);
        this.userStateService = userStateService;
    }

    @Override
    protected void processCommand(Update update) {
        Long chatId = update.message().chat().id();
        userStateService.updateUserState(chatId, UserState.MENU);
        userStateService.updateUserSubstate(chatId, UserState.SubState.START);
        telegramBotService.sendMessage(chatId, "Добро пожаловать в меню");
    }

    @Override
    public String getHandlerName() {
        return CommandNameEnum.START.getName();
    }
}
