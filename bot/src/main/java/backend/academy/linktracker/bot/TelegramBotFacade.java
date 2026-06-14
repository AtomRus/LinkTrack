package backend.academy.linktracker.bot;

import com.pengrad.telegrambot.ExceptionHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!test")
public class TelegramBotFacade implements ApplicationRunner {

    private final TelegramBot telegramBot;
    private final ExceptionHandler exceptionHandler;
    private final UpdatesListener updatesListener;

    @Override
    public void run(ApplicationArguments args) {

        try {
            telegramBot.setUpdatesListener(updatesListener, exceptionHandler);
        } catch (Exception e) {
            log.error("Произошла ошибка исполнения {}", e.toString());
        }
    }
}
