package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.state.AbstractState;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Сбор всех доступных состояний в мапу, чтобы пользователь мог за O(1) найти нужное состояние
@Configuration
public class StateConfig {
    @Bean
    public Map<UserState, AbstractState> strategyMap(List<AbstractState> stateList) {
        return stateList.stream().collect(Collectors.toMap(AbstractState::getState, abstractState -> abstractState));
    }
}
