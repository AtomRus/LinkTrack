package backend.academy.linktracker.bot.command.telegrambot;

import backend.academy.linktracker.bot.command.annotaion.Command;
import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.requirement.ContextKey;
import backend.academy.linktracker.bot.service.TelegramBotService;
import backend.academy.linktracker.bot.util.HandlerAnnotationUtils;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTelegramCommandHandler extends AbstractInputHandler {

    public AbstractTelegramCommandHandler(
            TelegramBotService telegramBotService, Map<String, AbstractRequirement> allRequirements) {
        super(telegramBotService, allRequirements);
    }

    protected List<String> defaultRequirementNames() {
        LinkedList<String> listRequirements = new LinkedList<>();
        listRequirements.add("textNotEmpty");
        listRequirements.add("textMatchCommand");
        return listRequirements;
    }

    @Override
    protected List<String> getRequirementNames() {
        return defaultRequirementNames();
    }

    @Override
    public String getHandlerName() {
        Command annotation = HandlerAnnotationUtils.findAnnotation(this, Command.class);
        if (annotation != null) {
            return annotation.command();
        }
        throw new IllegalStateException(
                "У обработчика " + this.getClass().getSimpleName() + " отсутствует аннотация @Command");
    }

    @Override
    public String getDescription() {
        Command annotation = HandlerAnnotationUtils.findAnnotation(this, Command.class);
        if (annotation != null) {
            return annotation.description();
        }
        throw new IllegalStateException(
                "У обработчика " + this.getClass().getSimpleName() + " некорретная аннотация @Command");
    }

    @Override
    protected Map<ContextKey, Object> getExtraData() {
        Command annotation = HandlerAnnotationUtils.findAnnotation(this, Command.class);
        if (annotation == null) {
            throw new IllegalStateException(
                    "У обработчика " + this.getClass().getSimpleName() + " отсутствует аннотация @Command");
        }
        HashMap<ContextKey, Object> map = new HashMap<>();
        map.put(ContextKey.COMMAND, annotation.command());
        return map;
    }
}
