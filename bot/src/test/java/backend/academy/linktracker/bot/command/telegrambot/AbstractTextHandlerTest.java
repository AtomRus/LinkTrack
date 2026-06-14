package backend.academy.linktracker.bot.command.telegrambot;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.bot.command.annotaion.TextHandler;
import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.service.TelegramBotService;
import com.pengrad.telegrambot.model.Update;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class AbstractTextHandlerTest {

    @Mock
    private TelegramBotService telegramBotService;

    @TextHandler(
            handlerName = "customName",
            description = "Custom text handler",
            states = {UserState.TRACKING})
    static class CustomTextHandler extends AbstractTextHandler {
        public CustomTextHandler(
                TelegramBotService telegramBotService, Map<String, AbstractRequirement> allRequirements) {
            super(telegramBotService, allRequirements);
        }

        @Override
        protected void processCommand(Update update) {}
    }

    @TextHandler(states = {UserState.TRACKING})
    static class FallbackNameHandler extends AbstractTextHandler {
        public FallbackNameHandler(
                TelegramBotService telegramBotService, Map<String, AbstractRequirement> allRequirements) {
            super(telegramBotService, allRequirements);
        }

        @Override
        protected void processCommand(Update update) {}
    }

    static class InvalidTextHandler extends AbstractTextHandler {
        public InvalidTextHandler(
                TelegramBotService telegramBotService, Map<String, AbstractRequirement> allRequirements) {
            super(telegramBotService, allRequirements);
        }

        @Override
        protected void processCommand(Update update) {}
    }

    @Test
    @DisplayName("Должен брать имя обработчика из аннотации")
    void shouldGetHandlerNameFromAnnotation() {
        Map<String, AbstractRequirement> requirements = Map.of();
        CustomTextHandler handler = new CustomTextHandler(telegramBotService, requirements);
        assertEquals("customName", handler.getHandlerName());
        assertEquals("Custom text handler", handler.getDescription());
        assertTrue(handler.getRequirementNames().contains("textNotEmpty"));
    }

    @Test
    @DisplayName("Должен генерировать имя обработчика на основе имени класса, если в аннотации пусто")
    void shouldGenerateFallbackName() {
        Map<String, AbstractRequirement> requirements = Map.of();
        FallbackNameHandler handler = new FallbackNameHandler(telegramBotService, requirements);
        assertEquals("fallbackNameHandler", handler.getHandlerName());
    }

    @Test
    @DisplayName("Должен выбрасывать исключение, если аннотация @TextHandler отсутствует")
    void shouldThrowExceptionIfAnnotationMissing() {
        Map<String, AbstractRequirement> requirements = Map.of();
        InvalidTextHandler handler = new InvalidTextHandler(telegramBotService, requirements);
        assertThrows(IllegalStateException.class, handler::getDescription);
    }
}
