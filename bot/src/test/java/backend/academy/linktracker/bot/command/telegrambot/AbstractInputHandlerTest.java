package backend.academy.linktracker.bot.command.telegrambot;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.service.TelegramBotService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AbstractInputHandlerTest {

    static class DummyInputHandler extends AbstractInputHandler {
        boolean processCommandCalled = false;

        public DummyInputHandler(
                TelegramBotService telegramBotService, Map<String, AbstractRequirement> allRequirements) {
            super(telegramBotService, allRequirements);
        }

        @Override
        protected void processCommand(Update update) {
            processCommandCalled = true;
        }

        @Override
        public String getHandlerName() {
            return "dummy";
        }

        @Override
        public String getDescription() {
            return "dummy desc";
        }

        @Override
        protected List<String> getRequirementNames() {
            return List.of("req1", "req2");
        }
    }

    private DummyInputHandler handler;

    @Mock
    private AbstractRequirement req1;

    @Mock
    private TelegramBotService telegramBotService;

    @Mock
    private AbstractRequirement req2;

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

        Map<String, AbstractRequirement> requirements = Map.of("req1", req1, "req2", req2);
        handler = new DummyInputHandler(telegramBotService, requirements);

        lenient().when(update.updateId()).thenReturn(1);
        lenient().when(update.message()).thenReturn(message);
        lenient().when(message.chat()).thenReturn(chat);
        lenient().when(message.from()).thenReturn(user);
        lenient().when(chat.id()).thenReturn(100L);
        lenient().when(user.id()).thenReturn(200L);
    }

    @Test
    @DisplayName("canHandle должен возвращать true, если все проверки пройдены")
    void shouldReturnTrueWhenAllRequirementsMet() {
        when(req1.handle(any())).thenReturn(true);
        when(req2.handle(any())).thenReturn(true);

        assertTrue(handler.canHandle(update));
    }

    @Test
    @DisplayName("canHandle должен возвращать false, если хотя бы одна проверка не пройдена")
    void shouldReturnFalseWhenOneRequirementFails() {
        when(req1.handle(any())).thenReturn(true);
        when(req2.handle(any())).thenReturn(false);

        assertFalse(handler.canHandle(update));
    }

    @Test
    @DisplayName("canHandle должен выбрасывать исключение, если проверка не найдена")
    void shouldThrowExceptionWhenRequirementNotFound() {
        Map<String, AbstractRequirement> requirements = Map.of("req1", req1, "req2", req2);
        DummyInputHandler badHandler = new DummyInputHandler(telegramBotService, requirements) {
            @Override
            protected List<String> getRequirementNames() {
                return List.of("unknownReq");
            }
        };
        ReflectionTestUtils.setField(badHandler, "allRequirements", Map.of());

        assertThrows(IllegalStateException.class, () -> badHandler.canHandle(update)); //
    }

    @Test
    @DisplayName("handle должен вызывать processCommand")
    void shouldCallProcessCommand() {
        handler.handle(update);
        assertTrue(handler.processCommandCalled); //
    }
}
