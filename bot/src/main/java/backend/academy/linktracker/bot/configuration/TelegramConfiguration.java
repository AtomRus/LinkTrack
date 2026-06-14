package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.properties.TelegramProperties;
import com.pengrad.telegrambot.ExceptionHandler;
import com.pengrad.telegrambot.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TelegramConfiguration {

    @Bean
    @ConditionalOnProperty(value = "app.telegram.enabled", havingValue = "true", matchIfMissing = true)
    public TelegramBot telegramBot(TelegramProperties properties) {
        var builder = new TelegramBot.Builder(properties.getToken())
                .apiUrl(properties.getUrl())
                .updateListenerSleep(properties.getUpdateListenerSleep().toMillis());

        if (properties.isDebug()) {
            builder.debug();
        }
        return builder.build();
    }

    @Bean
    public ExceptionHandler botExceptionHandler() {
        return e -> {
            if (e.response() != null) {
                MDC.put("error_code", String.valueOf(e.response().errorCode()));
                MDC.put("description", String.valueOf(e.response().description()));
                log.error("Ошибка Telegram API");
                e.printStackTrace();
                MDC.clear();
            } else {
                log.error("Необработанная ошибка при работе с Telegram", e);
            }
        };
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.telegram", name = "enabled", havingValue = "false")
    public TelegramBot offlineTelegramBot() {
        // Создаем бота с любым фейковым токеном.
        // Он не будет никуда подключаться, так как Facade (UpdatesListener)
        // у вас тоже выключен по профилю !test.
        return new TelegramBot("offline-mode-token");
    }
}
