package backend.academy.linktracker.bot.command.requirement.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.command.requirement.ContextKey;
import backend.academy.linktracker.bot.command.requirement.InputContext;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CommandRequirementTest {

    private TextMatchCommand requirement;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        requirement = new TextMatchCommand();
        when(update.message()).thenReturn(message);
    }

    @Test
    @DisplayName("Должен возвращать true, если текст совпадает с командой из контекста")
    void shouldReturnTrueWhenCommandMatches() {
        String command = "/track";
        when(message.text()).thenReturn(command);
        InputContext context = new InputContext(update, Map.of(ContextKey.COMMAND, command));

        assertTrue(requirement.isSatisfied(context));
    }

    @Test
    @DisplayName("Должен возвращать true для команды с суффиксом @botname")
    void shouldReturnTrueWhenCommandHasBotSuffix() {
        String command = "/track";
        when(message.text()).thenReturn("/track@link_tracker_bot");
        InputContext context = new InputContext(update, Map.of(ContextKey.COMMAND, command));

        assertTrue(requirement.isSatisfied(context));
    }

    @Test
    @DisplayName("Должен возвращать false, если текст не совпадает")
    void shouldReturnFalseWhenCommandDoesNotMatch() {
        when(message.text()).thenReturn("/help");
        InputContext context = new InputContext(update, Map.of(ContextKey.COMMAND, "/track"));

        assertFalse(requirement.isSatisfied(context));
    }

    @Test
    @DisplayName("Должен возвращать false, если команда отсутствует в контексте")
    void shouldReturnFalseWhenCommandMissingInContext() {
        when(message.text()).thenReturn("/track");
        InputContext context = new InputContext(update, Map.of()); // Пустая мапа данных

        assertFalse(requirement.isSatisfied(context));
    }
}
