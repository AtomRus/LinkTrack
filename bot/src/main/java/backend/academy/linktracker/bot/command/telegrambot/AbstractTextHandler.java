package backend.academy.linktracker.bot.command.telegrambot;

import backend.academy.linktracker.bot.command.annotaion.TextHandler;
import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.service.TelegramBotService;
import backend.academy.linktracker.bot.util.HandlerAnnotationUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;

@Slf4j
public abstract class AbstractTextHandler extends AbstractInputHandler {

    public AbstractTextHandler(
            TelegramBotService telegramBotService, Map<String, AbstractRequirement> allRequirements) {
        super(telegramBotService, allRequirements);
    }

    protected List<String> defaultRequirementNames() {
        return new ArrayList<>(List.of("textNotEmpty"));
    }

    @Override
    protected List<String> getRequirementNames() {
        return defaultRequirementNames();
    }

    @Override
    public String getHandlerName() {
        TextHandler annotation = HandlerAnnotationUtils.findAnnotation(this, TextHandler.class);
        if (annotation != null && !annotation.handlerName().isEmpty()) {
            return annotation.handlerName();
        }
        String className = AopUtils.getTargetClass(this).getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    @Override
    public String getDescription() {
        TextHandler annotation = HandlerAnnotationUtils.findAnnotation(this, TextHandler.class);
        if (annotation != null) {
            return annotation.description();
        }
        throw new IllegalStateException(
                "У обработчика " + this.getClass().getSimpleName() + " некорретная аннотация @TextHandler");
    }
}
