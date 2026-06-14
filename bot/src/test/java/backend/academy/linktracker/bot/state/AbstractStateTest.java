package backend.academy.linktracker.bot.state;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.service.CommandRegistryService;
import backend.academy.linktracker.bot.service.UserStateService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AbstractStateTest {

    static class DummyState extends AbstractState {
        boolean processStateCalled = false;

        @Override
        public UserState getState() {
            return UserState.MENU;
        }

        @Override
        public void onEnter(Long chatId) {}

        @Override
        protected void processState(Update update) {
            processStateCalled = true;
        }
    }

    private DummyState state;

    @Mock
    private CommandRegistryService commandRegistryService;

    @Mock
    private UserStateService userStateService;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @BeforeEach
    void setUp() {
        state = new DummyState();
        ReflectionTestUtils.setField(state, "commandRegistryService", commandRegistryService);
        ReflectionTestUtils.setField(state, "userStateService", userStateService);

        lenient().when(update.message()).thenReturn(message);
        lenient().when(message.chat()).thenReturn(chat);
        lenient().when(chat.id()).thenReturn(111L);
    }

    @Test
    @DisplayName("start должен получать хендлеры и вызывать processState")
    void shouldPrepareContextAndProcessState() {
        when(commandRegistryService.getCommandHandlerMapByState(UserState.MENU)).thenReturn(Map.of());
        when(commandRegistryService.getHandlerMapByState(UserState.MENU)).thenReturn(Map.of());
        when(userStateService.getCurrentUserSubstate(111L)).thenReturn(UserState.SubState.START);

        state.start(update);

        assertTrue(state.processStateCalled);
        verify(commandRegistryService).getCommandHandlerMapByState(UserState.MENU);
        verify(commandRegistryService).getHandlerMapByState(UserState.MENU);
        verify(userStateService).getCurrentUserSubstate(111L);
    }

    @Test
    @DisplayName("start не должен пробрасывать исключения наверх (перехватывать их)")
    void shouldCatchExceptionsDuringProcessing() {
        // Создаем стейт, который гарантированно упадет
        AbstractState failingState = new AbstractState() {
            @Override
            public void onEnter(Long chatId) {}

            @Override
            public UserState getState() {
                return UserState.MENU;
            }

            @Override
            protected void processState(Update update) {
                throw new RuntimeException("Test exception");
            }
        };
        ReflectionTestUtils.setField(failingState, "commandRegistryService", commandRegistryService);
        ReflectionTestUtils.setField(failingState, "userStateService", userStateService);

        when(userStateService.getCurrentUserSubstate(111L)).thenReturn(UserState.SubState.START);

        // Исключение не должно вылететь наружу, оно должно залогироваться в блоке catch
        failingState.start(update);
    }
}
