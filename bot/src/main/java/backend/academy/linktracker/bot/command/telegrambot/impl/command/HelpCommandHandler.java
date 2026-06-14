package backend.academy.linktracker.bot.command.telegrambot.impl.command;

import backend.academy.linktracker.bot.command.annotaion.Command;
import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.telegrambot.AbstractTelegramCommandHandler;
import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.service.TelegramBotService;
import com.pengrad.telegrambot.model.Update;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Command(
        command = "/help",
        description = "Помощь в работе с ботом",
        states = {UserState.MENU})
public class HelpCommandHandler extends AbstractTelegramCommandHandler {

    public HelpCommandHandler(TelegramBotService telegramBotService, Map<String, AbstractRequirement> allRequirements) {
        super(telegramBotService, allRequirements);
    }

    @Override
    protected void processCommand(Update update) {
        telegramBotService.sendMessage(
                update.message().chat().id(), "Посмотри в список доступных команд. Это большая кнопка 'Меню'");
    }
}
