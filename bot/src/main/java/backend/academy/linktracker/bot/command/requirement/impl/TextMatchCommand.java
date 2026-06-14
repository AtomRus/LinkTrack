package backend.academy.linktracker.bot.command.requirement.impl;

import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.requirement.ContextKey;
import backend.academy.linktracker.bot.command.requirement.InputContext;
import backend.academy.linktracker.bot.util.TelegramCommandUtils;
import org.springframework.stereotype.Component;

@Component
public class TextMatchCommand extends AbstractRequirement {
    @Override
    protected boolean isSatisfied(InputContext inputContext) {
        String command = inputContext.getMetadata(ContextKey.COMMAND, String.class);
        String text = inputContext.update().message().text();
        String actualCommand = TelegramCommandUtils.extractCommand(text);

        return actualCommand.equals(command) || text.startsWith(command + " ") || text.startsWith(command + "@");
    }

    @Override
    protected String getRequirementName() {
        return "textMatchCommand";
    }
}
