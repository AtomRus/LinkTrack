package backend.academy.linktracker.bot.state.impl;

import backend.academy.linktracker.bot.command.telegrambot.AbstractTelegramCommandHandler;
import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.command.telegrambot.impl.textHandler.TagsHandler;
import backend.academy.linktracker.bot.command.telegrambot.impl.textHandler.UriHandler;
import backend.academy.linktracker.bot.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.bot.grpc.ScrapperGrpcService;
import backend.academy.linktracker.bot.model.Link;
import backend.academy.linktracker.bot.service.LinkSessionService;
import backend.academy.linktracker.bot.service.TelegramBotService;
import backend.academy.linktracker.bot.service.UserStateService;
import backend.academy.linktracker.bot.state.AbstractState;
import backend.academy.linktracker.bot.util.TelegramCommandUtils;
import com.pengrad.telegrambot.model.Update;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD", "PMD.ExcessiveMethodLength", "PMD.DuplicatedCode"})
public class TrackingState extends AbstractState {
    private final TelegramBotService telegramBotService;
    private final UserStateService userStateService;
    private final ScrapperGrpcService scrapperGrpcService;
    private final LinkSessionService linkSessionService;

    @Override
    public UserState getState() {
        return UserState.TRACKING;
    }

    @Override
    protected void processState(Update update) {
        UserState.SubState userSubstate =
                userStateService.getCurrentUserSubstate(update.message().chat().id());
        switch (userSubstate) {
            case WAITING_FOR_URL -> handleURI(update);
            case WAITING_FOR_TAGS -> handleTags(update);
            default -> handleError(update);
        }
    }

    @Override
    public void onEnter(Long chatId) {
        telegramBotService.sendMessage(chatId, "Введите ссылку");
        userStateService.updateUserSubstate(chatId, UserState.SubState.WAITING_FOR_URL);
    }

    @SuppressWarnings("CPD-START")
    private void handleURI(Update update) {
        Long chatId = update.message().chat().id();
        UriHandler uriHandler = (UriHandler) handlerMap.get("uriHandler");
        AbstractTelegramCommandHandler abstractTelegramCommandHandler =
                commandHandlerMap.get(TelegramCommandUtils.extractCommand(update.message().text()));
        if (abstractTelegramCommandHandler != null) {
            abstractTelegramCommandHandler.handle(update);
        }
        if (uriHandler.canHandle(update)) {
            uriHandler.handle(update);
            userStateService.updateUserSubstate(chatId, UserState.SubState.WAITING_FOR_TAGS);
            telegramBotService.sendMessage(chatId, "Введите теги. Если не хотите, то напишите /skip");
        } else {
            telegramBotService.sendMessage(
                    chatId,
                    "Некорретная ссылка. Введите ссылку еще раз или введите /cancel, чтобы отменить процесс регистрирования");
        }
    }

    private void handleTags(Update update) {
        Long chatId = update.message().chat().id();
        if (Objects.equals(update.message().text(), "/skip")) {
            saveLink(update);
            return;
        }
        TagsHandler tagsHandler = (TagsHandler) handlerMap.get("tagsHandler");
        AbstractTelegramCommandHandler abstractTelegramCommandHandler =
                commandHandlerMap.get(TelegramCommandUtils.extractCommand(update.message().text()));
        if (abstractTelegramCommandHandler != null) {
            abstractTelegramCommandHandler.handle(update);
        }
        if (tagsHandler.canHandle(update)) {
            tagsHandler.handle(update);
            saveLink(update);
        } else {
            telegramBotService.sendMessage(
                    chatId,
                    "Некорретные теги." + " Введите ссылку еще раз или введите /cancel,"
                            + " чтобы отменить процесс регистрирования. Или введите /skip если передумали вводить теги");
        }
    }

    private void saveLink(Update update) {

        Long chatId = update.message().chat().id();
        Link link = linkSessionService.getSession(chatId);

        linkSessionService.deleteDraft(chatId);

        try {
            scrapperGrpcService.addLink(chatId, link);

            telegramBotService.sendMessage(chatId, "Ссылка успешно добавлена");
            userStateService.updateUserState(chatId, UserState.MENU);
            userStateService.updateUserSubstate(chatId, UserState.SubState.START);

        } catch (LinkAlreadyTrackedException e) {
            log.warn("Ошибка при добавлении ссылки для чата {}: {}", chatId, e.getMessage());

            telegramBotService.sendMessage(chatId, "Эта ссылка уже отслеживается");

            userStateService.updateUserState(chatId, UserState.MENU);
            userStateService.updateUserSubstate(chatId, UserState.SubState.START);
        } catch (Exception e) {
            telegramBotService.sendMessage(chatId, "Произошла непредвиденная ошибка при добавлении ссылки.");
            e.printStackTrace();
            userStateService.updateUserState(chatId, UserState.MENU);
            userStateService.updateUserSubstate(chatId, UserState.SubState.START);
        }
    }

    private void handleError(Update update) {
        Long chatId = update.message().chat().id();
        telegramBotService.sendMessage(chatId, "Ошибка исполнения команды. Возвращаю Вас в меню");
        userStateService.updateUserState(chatId, UserState.MENU);
        userStateService.updateUserSubstate(chatId, UserState.SubState.START);
    }
}
