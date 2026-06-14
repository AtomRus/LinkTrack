package backend.academy.linktracker.bot.command.telegrambot.impl.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.service.LinkSessionService;
import backend.academy.linktracker.bot.service.UserStateService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddTagCommandHandlerTest {

    @Mock
    private UserStateService userStateService;

    @Mock
    private LinkSessionService linkSessionService;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @InjectMocks
    private AddTagCommandHandler addTagCommandHandler;

    @Test
    void shouldSwitchStateToAddTagAndInitializeSession() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(10L);

        addTagCommandHandler.processCommand(update);

        verify(linkSessionService).updateSession(any());
        verify(userStateService).updateUserState(10L, UserState.ADD_TAG);
        verify(userStateService).updateUserSubstate(10L, UserState.SubState.START);
    }
}
