package backend.academy.linktracker.bot.command.telegrambot;

import lombok.Getter;

/**
 * Список всех команд. При старте приложения в TelegramConfiguration в методе botCommands()
 * будет использовать этот Enum для автоматического создания BotCommand[] и регистрацией их
 * в setMyCommands() в течении старта приложения
 */
@Getter
public enum CommandNameEnum {
    HELP("/help", "Получить список команд"),
    START("/start", "Начать работу с ботом"),
    REMOVE_TAG("/removeTag", "Удалить тег у ссылки"),
    ADD_TAG("/addTag", "Добавить тег ссылке"),
    TRACK("/track", "Начать отслеживать ссылку"),
    UNTRACK("/untrack", "Перестать отслеживать ссылку"),
    LIST("/list", "Получить список отслеживаемых ссылок");

    private final String name;
    private final String description;

    CommandNameEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
