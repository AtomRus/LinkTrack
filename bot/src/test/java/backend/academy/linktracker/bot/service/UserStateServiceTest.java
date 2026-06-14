package backend.academy.linktracker.bot.service;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.repository.InMemoryUserStateRepository;
import backend.academy.linktracker.bot.repository.UserStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserStateServiceTest {

    private final UserStateRepository userStateRepository = new InMemoryUserStateRepository();

    private UserStateService userStateService;

    @BeforeEach
    void setUp() {
        userStateService = new UserStateService(userStateRepository);
    }

    @Test
    @DisplayName("Должен создавать новую сессию пользователя со значениями по умолчанию")
    void shouldCreateUserSession() {
        Long chatId = 1L;
        userStateService.createUserSession(chatId);

        assertEquals(UserState.MENU, userStateService.getCurrentUserState(chatId));
        assertEquals(UserState.SubState.START, userStateService.getCurrentUserSubstate(chatId));
    }

    @Test
    @DisplayName("Должен обновлять и получать состояние пользователя")
    void shouldUpdateAndGetUserState() {
        Long chatId = 2L;
        userStateService.updateUserState(chatId, UserState.TRACKING);

        assertEquals(UserState.TRACKING, userStateService.getCurrentUserState(chatId));
    }

    @Test
    @DisplayName("Должен обновлять и получать подсостояние пользователя")
    void shouldUpdateAndGetUserSubstate() {
        Long chatId = 3L;
        userStateService.updateUserSubstate(chatId, UserState.SubState.WAITING_FOR_URL);

        assertEquals(UserState.SubState.WAITING_FOR_URL, userStateService.getCurrentUserSubstate(chatId));
    }
}
