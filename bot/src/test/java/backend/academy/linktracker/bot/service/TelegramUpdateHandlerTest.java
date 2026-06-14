package backend.academy.linktracker.bot.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.metrics.BotMetrics;
import backend.academy.linktracker.bot.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.bot.state.AbstractState;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SetMyCommands;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TelegramUpdateHandlerTest {

    @Mock
    private TelegramBotService telegramBotService;

    @Mock
    private CommandRegistryService commandRegistryService;

    @Mock
    private UserStateService userStateService;

    @Mock
    private BotMetrics botMetrics;

    @Mock
    private AbstractState menuState;

    @Mock
    private AbstractState trackingState;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    private TelegramUpdateHandler handler;

    private final Long CHAT_ID = 12345L;

    @BeforeEach
    void setUp() {
        Map<UserState, AbstractState> stateMap = Map.of(
                UserState.MENU, menuState,
                UserState.TRACKING, trackingState);
        handler = new TelegramUpdateHandler(
                telegramBotService, commandRegistryService, stateMap, userStateService, botMetrics);
    }

    @Test
    @DisplayName("Должен игнорировать Update без сообщения")
    void shouldIgnoreUpdateWithoutMessage() {
        when(update.message()).thenReturn(null);

        handler.process(List.of(update));

        verifyNoInteractions(userStateService, telegramBotService, commandRegistryService, menuState, trackingState);
    }

    @Test
    @DisplayName("Должен создать сессию и отправить приветствие для нового пользователя")
    void shouldCreateSessionForNewUser() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(CHAT_ID);
        when(userStateService.getCurrentUserState(CHAT_ID)).thenReturn(null).thenReturn(UserState.MENU);
        when(commandRegistryService.getSetMyCommandsByState(UserState.MENU)).thenReturn(mock(SetMyCommands.class));

        handler.process(List.of(update));

        verify(userStateService).createUserSession(CHAT_ID);
        verify(telegramBotService).sendMessage(CHAT_ID, "Приветствую тебя в моем боте!");
        verify(telegramBotService).setCommandMenu(eq(CHAT_ID), any());
    }

    @Test
    @DisplayName("Должен обработать update и вызвать onEnter при смене состояния")
    void shouldProcessUpdateAndHandleTransition() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(CHAT_ID);
        when(userStateService.getCurrentUserState(CHAT_ID))
                .thenReturn(UserState.MENU)
                .thenReturn(UserState.TRACKING);
        when(commandRegistryService.getSetMyCommandsByState(UserState.TRACKING)).thenReturn(mock(SetMyCommands.class));

        handler.process(List.of(update));

        verify(menuState).start(update);
        verify(trackingState).onEnter(CHAT_ID);
        verify(telegramBotService).setCommandMenu(eq(CHAT_ID), any());
    }

    @Test
    @DisplayName("Должен перехватывать бизнес-ошибку и выводить сообщение")
    void shouldCatchLinkAlreadyTrackedException() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(CHAT_ID);
        when(userStateService.getCurrentUserState(CHAT_ID)).thenReturn(UserState.MENU);

        doThrow(new LinkAlreadyTrackedException("Ссылка уже существует"))
                .when(menuState)
                .start(update);

        handler.process(List.of(update));

        verify(telegramBotService).sendMessage(CHAT_ID, "Ошибка: Ссылка уже существует");
    }

    @Test
    @DisplayName("Должен перехватывать RuntimeException и выводить generic сообщение")
    void shouldCatchGenericException() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(CHAT_ID);
        when(userStateService.getCurrentUserState(CHAT_ID)).thenReturn(UserState.MENU);

        doThrow(new RuntimeException("Something bad")).when(menuState).start(update);

        handler.process(List.of(update));

        verify(telegramBotService).sendMessage(CHAT_ID, "Произошла внутренняя ошибка сервера.");
    }
}
