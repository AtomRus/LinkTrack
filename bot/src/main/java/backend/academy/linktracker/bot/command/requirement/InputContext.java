package backend.academy.linktracker.bot.command.requirement;

import com.pengrad.telegrambot.model.Update;
import java.util.Map;

/**
 * Эта запись нужна, чтобы при изменений проверок не изменять интерфейс классов,
 * мы всегда будем во все проверки передавать CommandContext, а из него уже брать нужные данные.
 * Если для какой-то проверки потребуются особенные данные, которые не потребуются в остальные проверках,
 * то их можно положить в extraData, создав ключ в ContextKey, и когда нужно будет реализовать проверку,
 * где требуются особые данные, то их можно будет получить в классе проверки из extraData по ContextKey
 * @param update
 * @param extraData
 */
public record InputContext(Update update, Map<ContextKey, Object> extraData) {
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(ContextKey key, Class<T> clazz) {
        Object value = extraData.get(key);
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        return null;
    }
}
