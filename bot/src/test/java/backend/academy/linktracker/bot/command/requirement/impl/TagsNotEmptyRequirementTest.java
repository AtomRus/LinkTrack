package backend.academy.linktracker.bot.command.requirement.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.command.requirement.InputContext;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TagsNotEmptyRequirementTest {

    private TextIsTags requirement;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        requirement = new TextIsTags();
        when(update.message()).thenReturn(message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"tag", "  tag  "})
    @DisplayName("Должен возвращать true, если теги введены")
    void shouldReturnTrueWhenTagsPresent(String text) {
        when(message.text()).thenReturn(text);
        assertTrue(requirement.isSatisfied(new InputContext(update, Map.of())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  \n  "})
    @DisplayName("Должен возвращать false, если строка пуста")
    void shouldReturnFalseWhenTagsEmpty(String text) {
        when(message.text()).thenReturn(text);
        assertFalse(requirement.isSatisfied(new InputContext(update, Map.of())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1tag", "  tag32  ", " # fds"})
    @DisplayName("Должен возвращать false, если теги невалидные")
    void shouldReturnFalseWhenTagsIsInvalid(String text) {
        when(message.text()).thenReturn(text);
        assertFalse(requirement.isSatisfied(new InputContext(update, Map.of())));
    }
}
