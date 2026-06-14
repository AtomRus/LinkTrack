package backend.academy.linktracker.bot.command.telegrambot.impl.textHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.model.Link;
import backend.academy.linktracker.bot.service.LinkSessionService;
import backend.academy.linktracker.bot.service.UserStateService;
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
class TagsHandlerTest {

    @Mock
    private LinkSessionService linkSessionService;

    @Mock
    private UserStateService userStateService;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Test
    void shouldParseTagsTrimLowercaseAndDistinct() {
        TagsHandler handler = new TagsHandler(null, Map.of(), linkSessionService);
        Link sessionLink = new Link(1L, URI.create("https://example.com"), List.of());
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.text()).thenReturn(" Java, spring,java ,  kotlin ");
        when(linkSessionService.getSession(1L)).thenReturn(sessionLink);

        handler.processCommand(update);

        assertEquals(List.of("java", "spring", "kotlin"), sessionLink.getTags());
        verify(linkSessionService).updateSession(sessionLink);
    }

    @Test
    void shouldSetEmptyTagsForBlankInput() {
        TagsHandler handler = new TagsHandler(null, Map.of(), linkSessionService);
        Link sessionLink = new Link(2L, URI.create("https://example.com"), List.of("old"));
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(2L);
        when(message.text()).thenReturn("   ");
        when(linkSessionService.getSession(2L)).thenReturn(sessionLink);

        handler.processCommand(update);

        assertEquals(List.of(), sessionLink.getTags());
        verify(linkSessionService).updateSession(sessionLink);
    }
}
