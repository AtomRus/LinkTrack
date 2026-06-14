package backend.academy.linktracker.bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import backend.academy.linktracker.bot.command.annotaion.Command;
import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.telegrambot.AbstractTelegramCommandHandler;
import backend.academy.linktracker.bot.command.telegrambot.UserState;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CommandRegistryServiceProxyTest {

    @Command(
            command = "/help",
            description = "Help",
            states = {UserState.MENU})
    static class HelpHandler extends AbstractTelegramCommandHandler {
        HelpHandler() {
            super(mock(TelegramBotService.class), Map.of());
        }

        @Override
        protected void processCommand(Update update) {}
    }

    @Test
    void shouldResolveCommandsFromSubclassWithoutDirectAnnotation() {
        // Имитация CGLIB-прокси: аннотация на родителе, но не на runtime-классе
        AbstractTelegramCommandHandler proxyLikeHandler = new HelpHandler() {};

        CommandRegistryService registryService =
                new CommandRegistryService(List.of(proxyLikeHandler), List.of());

        Map<String, AbstractTelegramCommandHandler> map = registryService.getCommandHandlerMapByState(UserState.MENU);

        assertThat(map).containsKey("/help");
        assertThat(map.get("/help").getHandlerName()).isEqualTo("/help");
    }
}
