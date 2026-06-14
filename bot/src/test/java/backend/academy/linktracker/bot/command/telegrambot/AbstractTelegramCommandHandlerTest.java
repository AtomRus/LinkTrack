package backend.academy.linktracker.bot.command.telegrambot;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.bot.command.annotaion.Command;
import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.requirement.ContextKey;
import backend.academy.linktracker.bot.service.TelegramBotService;
import com.pengrad.telegrambot.model.Update;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class AbstractTelegramCommandHandlerTest {

    @Mock
    private TelegramBotService telegramBotService;

    @Command(
            command = "/test",
            description = "Test description",
            states = {UserState.MENU})
    static class ValidCommandHandler extends AbstractTelegramCommandHandler {
        public ValidCommandHandler(
                TelegramBotService telegramBotService, Map<String, AbstractRequirement> allRequirements) {
            super(telegramBotService, allRequirements);
        }

        @Override
        protected void processCommand(Update update) {}
    }

    static class InvalidCommandHandler extends AbstractTelegramCommandHandler {
        public InvalidCommandHandler(
                TelegramBotService telegramBotService, Map<String, AbstractRequirement> allRequirements) {
            super(telegramBotService, allRequirements);
        }

        @Override
        protected void processCommand(Update update) {}
    }

    @Test
    @DisplayName("Должен корректно читать параметры из аннотации @Command")
    void shouldReadAnnotationProperties() {

        Map<String, AbstractRequirement> requirements = Map.of();
        ValidCommandHandler handler = new ValidCommandHandler(telegramBotService, requirements);

        assertEquals("/test", handler.getHandlerName());
        assertEquals("Test description", handler.getDescription());
        assertEquals(2, handler.getRequirementNames().size());

        Map<ContextKey, Object> extraData = handler.getExtraData();
        assertEquals("/test", extraData.get(ContextKey.COMMAND)); //
    }

    @Test
    @DisplayName("Должен выбрасывать исключение, если аннотация @Command отсутствует")
    void shouldThrowExceptionIfAnnotationMissing() {
        Map<String, AbstractRequirement> requirements = Map.of();
        InvalidCommandHandler handler = new InvalidCommandHandler(telegramBotService, requirements);

        assertThrows(IllegalStateException.class, handler::getHandlerName);
        assertThrows(IllegalStateException.class, handler::getDescription);
    }
}
