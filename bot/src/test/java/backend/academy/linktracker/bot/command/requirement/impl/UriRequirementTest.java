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

class UriRequirementTest {

    private TextIsUri requirement;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        requirement = new TextIsUri();
        when(update.message()).thenReturn(message);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://github.com",
                "http://localhost:8080",
                "https://google.com/search?q=test",
            })
    @DisplayName("Должен возвращать true для валидных URI")
    void shouldReturnTrueForValidUri(String url) {
        when(message.text()).thenReturn(url);
        InputContext context = new InputContext(update, Map.of());

        assertTrue(requirement.isSatisfied(context));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-link", "just text with spaces", "://missingscheme.com"})
    @DisplayName("Должен возвращать false для невалидных строк")
    void shouldReturnFalseForInvalidUri(String invalidUrl) {
        when(message.text()).thenReturn(invalidUrl);
        InputContext context = new InputContext(update, Map.of());

        assertFalse(requirement.isSatisfied(context));
    }
}
