package backend.academy.linktracker.bot.command.telegrambot.impl.command;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.exception.ResourceNotFoundException;
import backend.academy.linktracker.bot.exception.TagNotFoundException;
import backend.academy.linktracker.bot.grpc.ScrapperGrpcService;
import backend.academy.linktracker.bot.model.Link;
import backend.academy.linktracker.bot.service.TelegramBotService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListCommandHandlerTest {
    @Mock
    private ScrapperGrpcService scrapperGrpcService;

    @Mock
    private TelegramBotService telegramBotService;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    private ListCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ListCommandHandler(telegramBotService, Map.of(), scrapperGrpcService);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
    }

    @Test
    void shouldShowNoLinksMessageWhenScrapperThrowsNotFound() {
        when(message.text()).thenReturn("/list");
        when(scrapperGrpcService.getLinks(1L)).thenThrow(new ResourceNotFoundException("not found"));

        handler.processCommand(update);

        verify(telegramBotService).sendMessage(1L, "Отслеживаемых ссылок нет");
    }

    @Test
    void shouldShowValidationMessageForIncorrectTag() {
        when(message.text()).thenReturn("/list java123");

        handler.processCommand(update);

        verify(telegramBotService).sendMessage(1L, "Введен некорректный тег! Используйте только буквы");
    }

    @Test
    void shouldShowTagNotFoundMessage() {
        when(message.text()).thenReturn("/list java");
        when(scrapperGrpcService.getLinksByTags(1L, "java")).thenThrow(new TagNotFoundException("not found"));

        handler.processCommand(update);

        verify(telegramBotService).sendMessage(1L, "По такому тегу ссылок нет!");
    }

    @Test
    void shouldSendLinksWhenNoTagFilter() {
        when(message.text()).thenReturn("/list");
        when(scrapperGrpcService.getLinks(1L))
                .thenReturn(List.of(new Link(10L, URI.create("https://example.com"), List.of("java"))));

        handler.processCommand(update);

        String expectedMessage = "Ваши ссылки:\n* https://example.com";
        verify(telegramBotService).sendMessage(eq(1L), eq(expectedMessage));
    }
}
