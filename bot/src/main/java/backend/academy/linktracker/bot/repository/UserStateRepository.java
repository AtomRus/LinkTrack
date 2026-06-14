package backend.academy.linktracker.bot.repository;

import backend.academy.linktracker.bot.command.telegrambot.UserState;

public interface UserStateRepository {
    void createUserSession(Long chatId);

    UserState getCurrentUserState(Long chatId);

    void updateUserState(Long chatId, UserState userState);

    UserState.SubState getCurrentUserSubstate(Long chatId);

    void updateUserSubstate(Long chatId, UserState.SubState subState);
}
