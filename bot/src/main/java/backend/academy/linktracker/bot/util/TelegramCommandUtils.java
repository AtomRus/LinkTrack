package backend.academy.linktracker.bot.util;

public final class TelegramCommandUtils {

    private TelegramCommandUtils() {}

    /**
     * Извлекает имя команды из текста сообщения.
     * Поддерживает форматы: {@code /help}, {@code /help@botname}, {@code /help arg}.
     */
    public static String extractCommand(String text) {
        if (text == null || text.isBlank() || !text.startsWith("/")) {
            return text;
        }
        String token = text.trim().split("\\s+", 2)[0];
        int atIndex = token.indexOf('@');
        if (atIndex > 0) {
            return token.substring(0, atIndex);
        }
        return token;
    }
}
