package backend.academy.linktracker.bot.command.telegrambot;

import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.requirement.ContextKey;
import backend.academy.linktracker.bot.command.requirement.InputContext;
import backend.academy.linktracker.bot.service.TelegramBotService;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * В приложении есть AbstractTelegramCommandHandler(это команды по типу /start, /help и тп, по сути это UI программы)
 * и AbstractTextHandler(это обработчики какого-либо текста от пользователя, например, обработка URI или тегов.
 * Но чтобы проверить подходит ли текст вводимый пользователем под нужные критерии, я использовал паттерн "цепочка проверок"
 * и если вызвать у команды canHandle(Update update), то текст входящего апдейта провериться по всей цепочке.
 * Чаще всего какая-либо проверка текста перед обработкой нужна именно AbstractTextHandler, но делать проверки можно и для
 * AbstractTelegramCommandHandler
 */
@Slf4j
@AllArgsConstructor
public abstract class AbstractInputHandler {
    protected TelegramBotService telegramBotService;

    /**
     * Применил модифицированный паттерн "цепочка проверок",
     * чтобы не пришлось каждый раз писать одинаковые проверки.
     * Проверки теперь нужно писать как отдельный класс(см. директорию requirement), а при реализации
     * команд можно оставить дефолтный список проверок или создать свой список,
     * по которому уже спринг будет проверять выполнять команду или нет
     */
    private Map<String, AbstractRequirement> allRequirements;

    protected List<String> getRequirementNames() {
        return null;
    }

    protected Map<ContextKey, Object> getExtraData() {
        return null;
    }

    public Boolean canHandle(Update update) {
        // Дополнительные данные от пользователя, которые потребуются в проверках условиях
        MDC.put("updateId", String.valueOf(update.updateId()));
        MDC.put("userId", String.valueOf(update.message().chat().id()));
        Map<ContextKey, Object> contextKeyObjectsMap = getExtraData();
        if (getRequirementNames() == null) {
            return true;
        }

        return getRequirementNames().stream()
                .map(name -> {
                    AbstractRequirement requirement = allRequirements.get(name);
                    if (requirement == null) {
                        throw new IllegalStateException("Требование не найдено: " + name);
                    }
                    return requirement;
                })
                // Спринг сформировал список проверок с проверками на null
                .allMatch(req -> req.handle(new InputContext(update, contextKeyObjectsMap)));
    }

    public void handle(Update update) {
        Long userId = update.message().from().id();
        Long chatId = update.message().chat().id();

        MDC.put("userId", String.valueOf(userId));
        MDC.put("chatId", String.valueOf(chatId));
        log.debug("Обработка  {}", getHandlerName());

        try {
            processCommand(update);
        } finally {
            MDC.clear();
        }
    }

    protected abstract void processCommand(Update update);

    public abstract String getHandlerName();

    public abstract String getDescription();
}
