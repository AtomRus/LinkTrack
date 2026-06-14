package backend.academy.linktracker.bot;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.command.telegrambot.AbstractTelegramCommandHandler;
import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.service.CommandRegistryService;
import backend.academy.linktracker.bot.service.TelegramBotService;
import backend.academy.linktracker.bot.service.UserStateService;
import backend.academy.linktracker.bot.state.impl.MenuState;
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
    private TelegramBotService telegramBotService;

    @Mock
    private CommandRegistryService registryService;

    @Mock
    private UserStateService userStateService;

    @Mock
    private AbstractTelegramCommandHandler helpCommand;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private User user;

    @Mock
    private Chat chat;

    @InjectMocks
    private MenuState menuState;

    private final Long USER_ID = 777L;

    @BeforeEach
    void setUp() {
        // Внедряем зависимости в абстрактный класс через Reflection
        ReflectionTestUtils.setField(menuState, "commandRegistryService", registryService);
        ReflectionTestUtils.setField(menuState, "userStateService", userStateService);

        lenient().when(update.message()).thenReturn(message);
        lenient().when(message.from()).thenReturn(user);
        lenient().when(message.chat()).thenReturn(chat);
        lenient().when(user.id()).thenReturn(USER_ID);
        lenient().when(chat.id()).thenReturn(USER_ID);
    }

    @Test
    @DisplayName("Должен выполнить команду, если она есть в реестре")
    void shouldExecuteCommandWhenMatchFound() {
        String commandText = "/help";
        when(message.text()).thenReturn(commandText);
        // Мокаем подсостояние START для MenuState
        when(userStateService.getCurrentUserSubstate(USER_ID)).thenReturn(UserState.SubState.START);
        when(registryService.getCommandHandlerMapByState(UserState.MENU)).thenReturn(Map.of(commandText, helpCommand));

        menuState.start(update);

        verify(helpCommand).handle(update);
    }

    @Test
    @DisplayName("Должен отправить сообщение об ошибке при неизвестной команде")
    void shouldSendUnknownCommandMessage() {
        when(message.text()).thenReturn("какой-то текст");
        when(userStateService.getCurrentUserSubstate(USER_ID)).thenReturn(UserState.SubState.START);
        // Возвращаем пустую мапу команд, имитируя, что такой команды нет
        when(registryService.getCommandHandlerMapByState(UserState.MENU)).thenReturn(Map.of());

        menuState.start(update);

        verify(telegramBotService).sendMessage(USER_ID, "Неизвестная команда, введите /help");
    }
}
