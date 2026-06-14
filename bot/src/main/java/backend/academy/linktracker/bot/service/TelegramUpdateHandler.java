package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.metrics.BotMetrics;
import backend.academy.linktracker.bot.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.bot.state.AbstractState;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramUpdateHandler implements UpdatesListener {

    private final TelegramBotService telegramBotService;
    private final CommandRegistryService commandRegistryService;
    private final Map<UserState, AbstractState> stateMap;
    private final UserStateService userStateService;
    private final BotMetrics botMetrics;

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() == null) {
                continue;
            }
            String text = update.message().text();
            String requestType = text != null && text.startsWith("/") ? "command" : "text";
            botMetrics.incrementUserMessage(requestType);
            Long chatId = update.message().chat().id();
            try {

                UserState oldState = userStateService.getCurrentUserState(chatId);
                if (oldState == null) {
                    telegramBotService.sendMessage(chatId, "Приветствую тебя в моем боте!");
                    userStateService.createUserSession(chatId);
                    oldState = userStateService.getCurrentUserState(chatId);
                    telegramBotService.setCommandMenu(chatId, commandRegistryService.getSetMyCommandsByState(oldState));
                    continue;
                }
                stateMap.get(oldState).start(update);

                UserState newState = userStateService.getCurrentUserState(chatId);

                // Проверяем перешел ли пользователь в другое состояние, если перешел, то мы передаем ему инструкции для
                // работы с новым состоянимем
                if (oldState != newState) {
                    stateMap.get(newState).onEnter(chatId);
                }

                telegramBotService.setCommandMenu(chatId, commandRegistryService.getSetMyCommandsByState(newState));
                MDC.clear();
            } catch (LinkAlreadyTrackedException e) {
                log.warn("Ошибка scrapper service в чате {}: {}", chatId, e.getMessage());
                telegramBotService.sendMessage(chatId, "Ошибка: " + e.getMessage());

            } catch (Exception e) {
                log.error("Критическая ошибка при обработке апдейта", e);
                telegramBotService.sendMessage(chatId, "Произошла внутренняя ошибка сервера.");
            }
        }
        return CONFIRMED_UPDATES_ALL;
    }
}
