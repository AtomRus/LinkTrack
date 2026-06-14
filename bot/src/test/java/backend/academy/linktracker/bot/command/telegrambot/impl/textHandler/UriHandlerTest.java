package backend.academy.linktracker.bot.command.telegrambot.impl.textHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.model.Link;
import backend.academy.linktracker.bot.service.LinkSessionService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UriHandlerTest {

    @Mock
    private LinkSessionService linkSessionService;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Test
    void shouldUpdateSessionLinkWithUriFromMessage() {
        UriHandler handler = new UriHandler(null, Map.of(), linkSessionService);
        Link sessionLink = new Link(1L, null, List.of());
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.text()).thenReturn("https://example.com/path");
        when(linkSessionService.getSession(1L)).thenReturn(sessionLink);

        handler.processCommand(update);

        assertEquals(URI.create("https://example.com/path"), sessionLink.getLink());
        verify(linkSessionService).updateSession(sessionLink);
    }
}
