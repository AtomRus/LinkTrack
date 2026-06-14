package backend.academy.linktracker.bot.command.telegrambot.impl.command;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.service.TelegramBotService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HelpCommandHandlerTest {

    @Mock
    private TelegramBotService telegramBotService;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Test
    void shouldSendHelpMessageToCurrentChat() {
        HelpCommandHandler handler = new HelpCommandHandler(telegramBotService, Map.of());
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);

        handler.processCommand(update);

        verify(telegramBotService).sendMessage(123L, "Посмотри в список доступных команд. Это большая кнопка 'Меню'");
    }
}
