package backend.academy.linktracker.bot.command.requirement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

@ExtendWith(MockitoExtension.class)
class AbstractRequirementTest {

    static class StubRequirement extends AbstractRequirement {
        boolean satisfiedValue = true;

        @Override
        protected boolean isSatisfied(InputContext inputContext) {
            return satisfiedValue;
        }

        @Override
        protected String getRequirementName() {
            return "stubRequirement";
        }
    }

    private StubRequirement requirement;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Mock
    private User user;

    @BeforeEach
    void setUp() {
        requirement = new StubRequirement();
        lenient().when(update.message()).thenReturn(message);
        lenient().when(message.chat()).thenReturn(chat);
        lenient().when(message.from()).thenReturn(user);
        lenient().when(chat.id()).thenReturn(100L);
        lenient().when(user.id()).thenReturn(200L);
    }

    @Test
    @DisplayName("handle должен устанавливать и очищать MDC")
    void shouldManageMDC() {
        InputContext context = new InputContext(update, Map.of());

        requirement.handle(context);

        // Проверяем, что после выполнения handle MDC пуст (очищен в finally)
        assertNull(MDC.get("requirementName"));
    }

    @Test
    @DisplayName("handle должен возвращать результат isSatisfied")
    void shouldReturnSatisfiedResult() {
        InputContext context = new InputContext(update, Map.of());

        requirement.satisfiedValue = true;
        assertTrue(requirement.handle(context));

        requirement.satisfiedValue = false;
        assertFalse(requirement.handle(context));
    }
}
