package backend.academy.linktracker.bot.state.impl;

import backend.academy.linktracker.bot.command.telegrambot.AbstractTelegramCommandHandler;
import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.command.telegrambot.impl.textHandler.TagsHandler;
import backend.academy.linktracker.bot.command.telegrambot.impl.textHandler.UriHandler;
import backend.academy.linktracker.bot.grpc.ScrapperGrpcService;
import backend.academy.linktracker.bot.model.Link;
import backend.academy.linktracker.bot.service.LinkSessionService;
import backend.academy.linktracker.bot.service.TelegramBotService;
import backend.academy.linktracker.bot.service.UserStateService;
import backend.academy.linktracker.bot.state.AbstractState;
import backend.academy.linktracker.bot.util.TelegramCommandUtils;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD", "PMD.ExcessiveMethodLength", "PMD.DuplicatedCode"})
public class RemoveTagState extends AbstractState {
    private final TelegramBotService telegramBotService;
    private final UserStateService userStateService;
    private final ScrapperGrpcService scrapperGrpcService;
    private final LinkSessionService linkSessionService;

    @Override
    public void onEnter(Long chatId) {

        telegramBotService.sendMessage(chatId, "Введите ссылку, у которой вы хотите удалить тег");
        userStateService.updateUserSubstate(chatId, UserState.SubState.WAITING_FOR_URL);
    }

    @Override
    public UserState getState() {
        return UserState.REMOVE_TAG;
    }

    @Override
    protected void processState(Update update) {
        UserState.SubState userSubstate =
                userStateService.getCurrentUserSubstate(update.message().chat().id());

        switch (userSubstate) {
            case WAITING_FOR_URL -> processUrl(update);
            case WAITING_FOR_TAG -> processTag(update);
            default -> handleError(update);
        }
    }

    private void processUrl(Update update) {
        Long chatId = update.message().chat().id();
        UriHandler uriHandler = (UriHandler) handlerMap.get("uriHandler");
        AbstractTelegramCommandHandler abstractTelegramCommandHandler =
                commandHandlerMap.get(TelegramCommandUtils.extractCommand(update.message().text()));
        if (abstractTelegramCommandHandler != null) {
            abstractTelegramCommandHandler.handle(update);
        }
        if (uriHandler.canHandle(update)) {
            uriHandler.handle(update);
            userStateService.updateUserSubstate(chatId, UserState.SubState.WAITING_FOR_TAG);
            telegramBotService.sendMessage(chatId, "Введите желаемый тег для удаления");
        } else {
            telegramBotService.sendMessage(
                    chatId,
                    "Некорретная ссылка. Введите ссылку еще раз или введите /cancel, чтобы отменить процесс удаления тега");
        }
    }

    private void processTag(Update update) {
        TagsHandler tagsHandler = (TagsHandler) handlerMap.get("tagsHandler");
        AbstractTelegramCommandHandler abstractTelegramCommandHandler =
                commandHandlerMap.get(TelegramCommandUtils.extractCommand(update.message().text()));
        if (abstractTelegramCommandHandler != null) {
            abstractTelegramCommandHandler.handle(update);
        }
        if (tagsHandler.canHandle(update)) {
            tagsHandler.handle(update);
            removeTag(update);
        } else {
            telegramBotService.sendMessage(
                    update.message().chat().id(),
                    "Некорретный тег." + " Введите тег еще раз или введите /cancel,"
                            + " чтобы отменить процесс удаления.");
        }
    }

    private void removeTag(Update update) {
        Long chatId = update.message().chat().id();
        Link link = linkSessionService.getSession(update.message().chat().id());

        scrapperGrpcService.removeTag(
                update.message().chat().id(),
                link.getLink().toString(),
                link.getTags().getFirst());
        telegramBotService.sendMessage(chatId, "Тег удален");
        userStateService.updateUserState(chatId, UserState.MENU);
        userStateService.updateUserSubstate(chatId, UserState.SubState.START);
    }

    private void handleError(Update update) {
        Long chatId = update.message().chat().id();
        telegramBotService.sendMessage(chatId, "Ошибка исполнения команды. Возвращаю Вас в меню");
        userStateService.updateUserState(chatId, UserState.MENU);
        userStateService.updateUserSubstate(chatId, UserState.SubState.START);
    }
}
