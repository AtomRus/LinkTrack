package backend.academy.linktracker.bot.state.impl;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.telegrambot.impl.textHandler.UriHandler;
import backend.academy.linktracker.bot.service.CommandRegistryService;
import backend.academy.linktracker.bot.service.TelegramBotService;
import backend.academy.linktracker.bot.service.UserStateService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TrackingStateTest {

    @Mock
    private CommandRegistryService registryService;

    @Mock
    private UserStateService userStateService;

    @Mock
    private TelegramBotService telegramBotService;

    @Mock
    private UriHandler uriHandler;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @InjectMocks
    private TrackingState trackingState;

    private final Long CHAT_ID = 555L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(trackingState, "commandRegistryService", registryService);
        ReflectionTestUtils.setField(trackingState, "userStateService", userStateService);
        ReflectionTestUtils.setField(trackingState, "telegramBotService", telegramBotService);

        lenient().when(update.message()).thenReturn(message);
        lenient().when(message.chat()).thenReturn(chat);
        lenient().when(chat.id()).thenReturn(CHAT_ID);
    }
}
