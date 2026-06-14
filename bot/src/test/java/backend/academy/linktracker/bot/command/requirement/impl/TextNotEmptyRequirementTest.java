package backend.academy.linktracker.bot.command.requirement.impl;

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

class TextNotEmptyRequirementTest {

    private TextNotEmpty requirement;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        requirement = new TextNotEmpty();
        when(update.message()).thenReturn(message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", " /start", "https://google.com"})
    @DisplayName("Должен возвращать true для непустого текста")
    void shouldReturnTrueForValidText(String text) {
        when(message.text()).thenReturn(text);
        InputContext context = new InputContext(update, Map.of());

        assertTrue(requirement.isSatisfied(context));
    }
}
