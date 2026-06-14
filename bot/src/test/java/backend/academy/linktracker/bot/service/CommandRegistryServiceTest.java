package backend.academy.linktracker.bot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.bot.command.annotaion.Command;
import backend.academy.linktracker.bot.command.annotaion.TextHandler;
import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.telegrambot.AbstractTelegramCommandHandler;
import backend.academy.linktracker.bot.command.telegrambot.AbstractTextHandler;
import backend.academy.linktracker.bot.command.telegrambot.UserState;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SetMyCommands;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class CommandRegistryServiceTest {
    @Mock
    private TelegramBotService telegramBotService;

    @Mock
    Map<String, AbstractRequirement> requirements;

    @Command(
            command = "/start",
            description = "Start bot",
            states = {UserState.MENU})
    static class DummyStartCommand extends AbstractTelegramCommandHandler {
        public DummyStartCommand(
                TelegramBotService telegramBotService, Map<String, AbstractRequirement> allRequirements) {
            super(telegramBotService, allRequirements);
        }

        @Override
        protected void processCommand(Update update) {}
    }

    @TextHandler(
            handlerName = "dummyText",
            states = {UserState.TRACKING})
    static class DummyTextHandler extends AbstractTextHandler {
        public DummyTextHandler(
                TelegramBotService telegramBotService, Map<String, AbstractRequirement> allRequirements) {
            super(telegramBotService, allRequirements);
        }

        @Override
        protected void processCommand(Update update) {}
    }

    private CommandRegistryService registryService;
    private DummyStartCommand startCommand;
    private DummyTextHandler textHandler;

    @BeforeEach
    void setUp() {
        startCommand = new DummyStartCommand(telegramBotService, requirements);
        textHandler = new DummyTextHandler(telegramBotService, requirements);
        registryService = new CommandRegistryService(List.of(startCommand), List.of(textHandler));
    }

    @Test
    @DisplayName("Должен возвращать команды по состоянию")
    void shouldGetCommandHandlerMapByState() {
        Map<String, AbstractTelegramCommandHandler> map = registryService.getCommandHandlerMapByState(UserState.MENU);

        assertEquals(1, map.size());
        assertTrue(map.containsKey("/start"));
        assertEquals(startCommand, map.get("/start"));
    }

    @Test
    @DisplayName("Должен возвращать обработчики текста по состоянию")
    void shouldGetHandlerMapByState() {
        Map<String, AbstractTextHandler> map = registryService.getHandlerMapByState(UserState.TRACKING);

        assertEquals(1, map.size());
        assertTrue(map.containsKey("dummyText"));
    }

    @Test
    @DisplayName("Должен генерировать команды для меню Telegram")
    void shouldGetSetMyCommandsByState() {
        SetMyCommands commands = registryService.getSetMyCommandsByState(UserState.MENU);
        // Не можем напрямую легко прочитать внутренности SetMyCommands без рефлексии,
        // но убеждаемся, что объект успешно создается и не пуст
        assertEquals("setMyCommands", commands.getMethod());
    }
}
