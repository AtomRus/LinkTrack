package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.command.annotaion.Command;
import backend.academy.linktracker.bot.command.annotaion.TextHandler;
import backend.academy.linktracker.bot.command.telegrambot.AbstractTelegramCommandHandler;
import backend.academy.linktracker.bot.command.telegrambot.AbstractTextHandler;
import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.util.HandlerAnnotationUtils;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CommandRegistryService {

    private final Map<String, AbstractTelegramCommandHandler> commandHandlerMap;
    private final List<AbstractTextHandler> handlerList;

    public CommandRegistryService(
            List<AbstractTelegramCommandHandler> availableCommands, List<AbstractTextHandler> availableHandlers) {
        commandHandlerMap = availableCommands.stream()
                .collect(Collectors.toMap(
                        AbstractTelegramCommandHandler::getHandlerName, commandHandler -> commandHandler));
        handlerList = availableHandlers;
    }

    public Map<String, AbstractTelegramCommandHandler> getCommandHandlerMapByState(UserState userState) {
        return commandHandlerMap.values().stream()
                .filter(commandHandler -> {
                    Command annotation = HandlerAnnotationUtils.findAnnotation(commandHandler, Command.class);
                    return annotation != null
                            && Arrays.asList(annotation.states()).contains(userState);
                })
                .collect(Collectors.toMap(
                        AbstractTelegramCommandHandler::getHandlerName,
                        handler -> handler,
                        (existing, replacement) -> existing));
    }

    public Map<String, AbstractTextHandler> getHandlerMapByState(UserState userState) {
        return handlerList.stream()
                .filter(handler -> {
                    TextHandler textHandler = HandlerAnnotationUtils.findAnnotation(handler, TextHandler.class);
                    return textHandler != null
                            && Arrays.asList(textHandler.states()).contains(userState);
                })
                .collect(Collectors.toMap(
                        AbstractTextHandler::getHandlerName, handel -> handel, (existing, handler) -> existing));
    }

    public SetMyCommands getSetMyCommandsByState(UserState userState) {
        BotCommand[] commands = commandHandlerMap.values().stream()
                .filter(Objects::nonNull)
                .filter(handler -> {
                    Command command = HandlerAnnotationUtils.findAnnotation(handler, Command.class);
                    return command != null && Arrays.asList(command.states()).contains(userState);
                })
                .map(handler -> new BotCommand(handler.getHandlerName(), handler.getDescription()))
                .toArray(BotCommand[]::new);

        return new SetMyCommands(commands);
    }
}
