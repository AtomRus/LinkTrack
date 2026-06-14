package backend.academy.linktracker.bot.command.telegrambot;

// UserState нужны для работы с AbstractState, а Substate нужны для исполнения команды внутри состояния
public enum UserState {
    MENU,
    ADD_TAG,
    REMOVE_TAG,
    TRACKING,
    UNTRACKING,
    LIST;

    public enum SubState {
        START,
        WAITING_FOR_URL,
        WAITING_FOR_TAG,
        WAITING_FOR_TAGS,
        CONFIRMATION
    }
}
