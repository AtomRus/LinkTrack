package backend.academy.linktracker.bot.state.impl;

import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.service.TelegramBotService;
import backend.academy.linktracker.bot.state.AbstractState;
import backend.academy.linktracker.bot.util.TelegramCommandUtils;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD", "PMD.ExcessiveMethodLength", "PMD.DuplicatedCode"})
public class MenuState extends AbstractState {
    private final TelegramBotService telegramBotService;

    @Override
    public UserState getState() {
        return UserState.MENU;
    }

    @Override
    protected void processState(Update update) {
        UserState.SubState userSubstate =
                userStateService.getCurrentUserSubstate(update.message().chat().id());

        switch (userSubstate) {
            case START -> processCommand(update);
            default -> handleError(update);
        }
    }

    @Override
    public void onEnter(Long chatId) {
        telegramBotService.sendMessage(chatId, "Добро пожаловать в меню");
        userStateService.updateUserSubstate(chatId, UserState.SubState.START);
    }

    @SuppressWarnings("CPD-START")
    private void processCommand(Update update) {
        String command = TelegramCommandUtils.extractCommand(update.message().text());

        if (commandHandlerMap.containsKey(command)) {
            commandHandlerMap.get(command).handle(update);
            return;
        }
        Long chatId = update.message().chat().id();
        telegramBotService.sendMessage(chatId, "Неизвестная команда, введите /help");
    }

    private void handleError(Update update) {
        log.error(
                "У пользователя {} отсутствует нужное подсостояние для состояния {}",
                update.message().chat().id(),
                getState());
        throw new RuntimeException("У пользователя " + update.message().chat().id()
                + " отсутствует нужное подсостояние для состояния " + getState());
    }
}
