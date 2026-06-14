package backend.academy.linktracker.bot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.metrics.BotMetrics;
import backend.academy.linktracker.bot.model.LinkUpdateModel;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TelegramBotServiceTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private BotMetrics botMetrics;

    @InjectMocks
    private TelegramBotService telegramBotService;

    @Test
    @DisplayName("Должен отправлять текстовое сообщение")
    void shouldSendMessage() {
        Long chatId = 123L;
        String text = "Test message";

        telegramBotService.sendMessage(chatId, text);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());

        SendMessage captured = captor.getValue();
        assertEquals(chatId, captured.getParameters().get("chat_id"));
        assertEquals(text, captured.getParameters().get("text"));
    }

    @Test
    @DisplayName("Должен устанавливать меню команд")
    void shouldSetCommandMenu() {
        Long chatId = 456L;
        SetMyCommands commands = new SetMyCommands(new BotCommand("/start", "Start bot"));

        telegramBotService.setCommandMenu(chatId, commands);

        ArgumentCaptor<SetMyCommands> captor = ArgumentCaptor.forClass(SetMyCommands.class);
        verify(telegramBot).execute(captor.capture());

        SetMyCommands captured = captor.getValue();
        assertNotNull(captured.getParameters().get("scope"));
    }

    @Test
    @DisplayName("Должен рассылать уведомления пользователям")
    void shouldNotifyUsers() {
        LinkUpdateModel model = new LinkUpdateModel();
        model.setUrl(URI.create("https://test.com"));
        model.setTgChatIds(List.of(10L, 20L));

        telegramBotService.notifyUsers(model);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot, times(2)).execute(captor.capture());

        List<SendMessage> messages = captor.getAllValues();
        assertEquals(10L, messages.get(0).getParameters().get("chat_id"));
        assertEquals(20L, messages.get(1).getParameters().get("chat_id"));
        assertEquals(
                "Произошло обновление по ссылке https://test.com",
                messages.get(0).getParameters().get("text"));
    }
}
