package backend.academy.linktracker.bot.repository;

import backend.academy.linktracker.bot.command.telegrambot.UserState;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryUserStateRepository implements UserStateRepository {
    private final Map<Long, UserState> userStateMap = new ConcurrentHashMap<>();
    private final Map<Long, UserState.SubState> userSubstateMap = new ConcurrentHashMap<>();

    public void createUserSession(Long chatId) {
        userStateMap.put(chatId, UserState.MENU);
        userSubstateMap.put(chatId, UserState.SubState.START);
    }

    public UserState getCurrentUserState(Long chatId) {
        return userStateMap.get(chatId);
    }

    public void updateUserState(Long chatId, UserState userState) {
        userStateMap.put(chatId, userState);
    }

    public UserState.SubState getCurrentUserSubstate(Long chatId) {
        return userSubstateMap.get(chatId);
    }

    public void updateUserSubstate(Long chatId, UserState.SubState subState) {
        userSubstateMap.put(chatId, subState);
    }
}
