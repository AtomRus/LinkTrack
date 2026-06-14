package backend.academy.linktracker.bot.state.impl;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.telegrambot.impl.textHandler.UriHandler;
import backend.academy.linktracker.bot.grpc.ScrapperGrpcService;
import backend.academy.linktracker.bot.service.CommandRegistryService;
import backend.academy.linktracker.bot.service.LinkSessionService;
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
class UntrackingStateTest {

    @Mock
    private UserStateService userStateService;

    @Mock
    private CommandRegistryService registryService;

    @Mock
    private TelegramBotService telegramBotService;

    @Mock
    private UriHandler uriHandler;

    @Mock
    private ScrapperGrpcService scrapperGrpcService;

    @Mock
    private LinkSessionService linkSessionService;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @InjectMocks
    private UntrackingState untrackingState;

    private final Long CHAT_ID = 999L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(untrackingState, "userStateService", userStateService);
        ReflectionTestUtils.setField(untrackingState, "commandRegistryService", registryService);
        ReflectionTestUtils.setField(untrackingState, "telegramBotService", telegramBotService);
        ReflectionTestUtils.setField(untrackingState, "scrapperGrpcService", scrapperGrpcService);
        ReflectionTestUtils.setField(untrackingState, "linkSessionService", linkSessionService);

        lenient().when(update.message()).thenReturn(message);
        lenient().when(message.chat()).thenReturn(chat);
        lenient().when(chat.id()).thenReturn(CHAT_ID);
    }

}
