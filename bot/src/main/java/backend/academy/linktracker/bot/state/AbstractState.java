package backend.academy.linktracker.bot.state;

import backend.academy.linktracker.bot.command.telegrambot.AbstractTelegramCommandHandler;
import backend.academy.linktracker.bot.command.telegrambot.AbstractTextHandler;
import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.service.CommandRegistryService;
import backend.academy.linktracker.bot.service.UserStateService;
import com.pengrad.telegrambot.model.Update;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * AbstractState описывает как должен проходить flow исполнения команды.
 * Чтобы юзер мог попадать в разные состояния используется UserState. А чтобы юзер мог идти по этапам исполнения внутри
 * одного состояния используется UserState.Substate. При создании команды указывается аннотация, где есть перечисление
 * всех UserState, в которых создаваемая команда должна быть. При старте приложения все команды инжектятся
 * в CommandRegistryService. Откуда абстракное состояние получает список комманд по методу getCommandHandlerMapByState(Userstate)
 * и обработчики текста по методу getHandlerMapByState(userState) в зависимости от состояния
 */
@Slf4j
@SuppressWarnings({"CPD-START", "CPD-END"})
public abstract class AbstractState {
    // Стоит подумать как сделать так, чтобы сам спринг инжектировал нужные реализации обработчиков с учетом состояния
    @Autowired
    private CommandRegistryService commandRegistryService;

    @Autowired
    protected UserStateService userStateService;

    protected Map<String, AbstractTelegramCommandHandler> commandHandlerMap;
    protected Map<String, AbstractTextHandler> handlerMap;

    public void start(Update update) {
        UserState userState = getState();
        commandHandlerMap = commandRegistryService.getCommandHandlerMapByState(userState);
        handlerMap = commandRegistryService.getHandlerMapByState(userState);
        UserState.SubState subState =
                userStateService.getCurrentUserSubstate(update.message().chat().id());
        try {
            MDC.put("chatId", String.valueOf(update.message().chat().id()));
            MDC.put("state", getState().toString());
            MDC.put("substate", subState.toString());
            processState(update);
        } catch (Exception e) {
            log.warn("Ошибка в состоянии {}: ", getState(), e);
        }
    }

    /**
     * Этот метод необходим, чтобы при вызове команд у пользователя изменилось состояние, и
     * при обработке этого же апдейта юзер получил бы инструкции к следующему состоянию
     * @param chatId
     */
    public abstract void onEnter(Long chatId);

    public abstract UserState getState();

    protected abstract void processState(Update update);
}
