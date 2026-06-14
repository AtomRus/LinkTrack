package backend.academy.linktracker.bot.command.requirement;

import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public abstract class AbstractRequirement {
    public boolean handle(InputContext inputContext) {
        Update update = inputContext.update();
        Long userId = update.message().from().id();
        Long chatId = update.message().chat().id();

        MDC.put("userId", String.valueOf(userId));
        MDC.put("chatId", String.valueOf(chatId));
        MDC.put("requirementName", getRequirementName());
        MDC.put("updateId", String.valueOf(inputContext.update().updateId()));
        log.debug("Проверка условия {}", getRequirementName());

        try {
            return isSatisfied(inputContext);
        } finally {
            MDC.clear();
        }
    }

    protected abstract boolean isSatisfied(InputContext inputContext);

    protected abstract String getRequirementName();
}
