package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.repository.UserStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * В этом сервисе проходит работа с состоянием и подсостоянием пользователя
 */
@Service
@RequiredArgsConstructor
public class UserStateService {
    private final UserStateRepository userStateRepository;

    public void createUserSession(Long chatId) {
        userStateRepository.createUserSession(chatId);
    }

    public UserState getCurrentUserState(Long chatId) {
        return userStateRepository.getCurrentUserState(chatId);
    }

    public void updateUserState(Long chatId, UserState userState) {
        userStateRepository.updateUserState(chatId, userState);
    }

    public UserState.SubState getCurrentUserSubstate(Long chatId) {
        return userStateRepository.getCurrentUserSubstate(chatId);
    }

    public void updateUserSubstate(Long chatId, UserState.SubState subState) {
        userStateRepository.updateUserSubstate(chatId, subState);
    }
}
