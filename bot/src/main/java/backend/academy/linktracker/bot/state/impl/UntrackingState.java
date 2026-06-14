package backend.academy.linktracker.bot.state.impl;

import backend.academy.linktracker.bot.command.telegrambot.AbstractTelegramCommandHandler;
import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.command.telegrambot.impl.textHandler.UriHandler;
import backend.academy.linktracker.bot.exception.ResourceNotFoundException;
import backend.academy.linktracker.bot.grpc.ScrapperGrpcService;
import backend.academy.linktracker.bot.model.Link;
import backend.academy.linktracker.bot.service.LinkSessionService;
import backend.academy.linktracker.bot.service.TelegramBotService;
import backend.academy.linktracker.bot.service.UserStateService;
import backend.academy.linktracker.bot.state.AbstractState;
import backend.academy.linktracker.bot.util.TelegramCommandUtils;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@SuppressWarnings({"CPD-START", "CPD-END"})
public class UntrackingState extends AbstractState {
    private final TelegramBotService telegramBotService;
    private final UserStateService userStateService;
    private final ScrapperGrpcService scrapperGrpcService;
    private final LinkSessionService linkSessionService;

    @Override
    public UserState getState() {
        return UserState.UNTRACKING;
    }

    @Override
    protected void processState(Update update) {
        UserState.SubState userSubstate =
                userStateService.getCurrentUserSubstate(update.message().chat().id());
        switch (userSubstate) {
            case WAITING_FOR_URL -> handleURI(update);
            case CONFIRMATION -> removeLink(update);
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
            userStateService.updateUserSubstate(chatId, UserState.SubState.CONFIRMATION);
            removeLink(update);
        } else {
            telegramBotService.sendMessage(
                    chatId,
                    "Некорретная ссылка. Введите ссылку еще раз или введите /cancel, чтобы отменить процесс удаления");
        }
    }

    private void removeLink(Update update) {
        Long chatId = update.message().chat().id();
        Link link = linkSessionService.getSession(chatId);
        linkSessionService.deleteDraft(chatId);
        try {
            scrapperGrpcService.removeLink(chatId, link);
        } catch (ResourceNotFoundException e) {
            telegramBotService.sendMessage(chatId, "Эта ссылка не отслеживается");
            userStateService.updateUserState(chatId, UserState.MENU);
            userStateService.updateUserSubstate(chatId, UserState.SubState.START);

        } catch (Exception e) {
            telegramBotService.sendMessage(
                    chatId, "Ошибка соединения, повторите свой запрос позже. Возвращаю Вас в меню");
            userStateService.updateUserState(chatId, UserState.MENU);
            userStateService.updateUserSubstate(chatId, UserState.SubState.START);
        }
        userStateService.updateUserState(chatId, UserState.MENU);
        userStateService.updateUserSubstate(chatId, UserState.SubState.START);
        telegramBotService.sendMessage(chatId, "Ссылка успешно удалена");
    }

    private void handleError(Update update) {
        Long chatId = update.message().chat().id();
        telegramBotService.sendMessage(chatId, "Ошибка исполнения команды. Возвращаю Вас в меню");
        userStateService.updateUserState(chatId, UserState.MENU);
        userStateService.updateUserSubstate(chatId, UserState.SubState.START);
    }
}
