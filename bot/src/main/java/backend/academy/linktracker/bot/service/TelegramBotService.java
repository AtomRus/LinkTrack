package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.metrics.BotMetrics;
import backend.academy.linktracker.bot.model.LinkUpdateModel;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandsScopeChat;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramBotService {
    private final TelegramBot telegramBot;
    private final BotMetrics botMetrics;

    public void sendMessage(Long chatId, String message) {
        telegramBot.execute(new SendMessage(chatId, message));
    }

    public void setCommandMenu(Long chatId, SetMyCommands botCommands) {
        telegramBot.execute(botCommands.scope(new BotCommandsScopeChat(chatId)));
    }

    public void notifyUsers(LinkUpdateModel linkUpdateModel) {
        for (Long chatId : linkUpdateModel.getTgChatIds()) {
            String prefix = linkUpdateModel.getUrl() != null
                    ? "Произошло обновление по ссылке " + linkUpdateModel.getUrl() + "\n"
                    : "";
            if (linkUpdateModel.getDescription() != null) {
                sendMessage(chatId, prefix + linkUpdateModel.getDescription());
                botMetrics.incrementSentNotification();
            } else if (linkUpdateModel.getUrl() != null) {
                sendMessage(chatId, prefix.trim());
                botMetrics.incrementSentNotification();
            }
        }
    }
}
