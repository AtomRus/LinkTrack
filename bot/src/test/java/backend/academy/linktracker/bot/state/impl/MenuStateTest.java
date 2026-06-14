package backend.academy.linktracker.bot.state.impl;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.telegrambot.AbstractTelegramCommandHandler;
import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.service.CommandRegistryService;
import backend.academy.linktracker.bot.service.TelegramBotService;
import backend.academy.linktracker.bot.service.UserStateService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MenuStateTest {

    @Mock
    private CommandRegistryService registryService;

    @Mock
    private UserStateService userStateService;

    @Mock
    private TelegramBotService telegramBotService;

    @Mock
    private AbstractTelegramCommandHandler helpCommand;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Mock
    private User user;

    @InjectMocks
    private MenuState menuState;

    private final Long CHAT_ID = 123L;

    @BeforeEach
    void setUp() {
        // Поскольку зависимости в AbstractState на @Autowired полях
        ReflectionTestUtils.setField(menuState, "commandRegistryService", registryService);
        ReflectionTestUtils.setField(menuState, "userStateService", userStateService);
        ReflectionTestUtils.setField(menuState, "telegramBotService", telegramBotService);

        lenient().when(update.message()).thenReturn(message);
        lenient().when(message.chat()).thenReturn(chat);
        lenient().when(chat.id()).thenReturn(CHAT_ID);
    }

    @Test
    @DisplayName("MenuState должен выполнять команду, если она найдена в реестре")
    void shouldExecuteCommandWhenFound() {
        String text = "/help";
        when(message.text()).thenReturn(text);
        when(userStateService.getCurrentUserSubstate(CHAT_ID)).thenReturn(UserState.SubState.START);
        when(registryService.getCommandHandlerMapByState(UserState.MENU)).thenReturn(Map.of(text, helpCommand));

        menuState.start(update);

        verify(helpCommand).handle(update);
    }

    @Test
    @DisplayName("MenuState должен отправлять ошибку, если команда не распознана")
    void shouldSendErrorWhenCommandNotFound() {
        lenient().when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("непонятный текст");
        when(userStateService.getCurrentUserSubstate(CHAT_ID)).thenReturn(UserState.SubState.START);
        when(registryService.getCommandHandlerMapByState(UserState.MENU)).thenReturn(Map.of());

        menuState.start(update);

        verify(telegramBotService).sendMessage(eq(CHAT_ID), contains("Неизвестная команда, введите /help"));
    }
}
