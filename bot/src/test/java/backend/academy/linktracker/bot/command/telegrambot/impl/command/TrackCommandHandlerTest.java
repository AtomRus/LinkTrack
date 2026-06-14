package backend.academy.linktracker.bot.command.telegrambot.impl.command;

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
class TrackCommandHandlerTest {
    @Mock
    private UserStateService userStateService;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Mock
    private LinkSessionService linkSessionService;

    @InjectMocks
    private TrackCommandHandler trackCommand;

    @Test
    void shouldChangeStateToTracking() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        trackCommand.processCommand(update);

        verify(userStateService).updateUserState(1L, UserState.TRACKING);
        verify(userStateService).updateUserSubstate(1L, UserState.SubState.START);
    }
}
