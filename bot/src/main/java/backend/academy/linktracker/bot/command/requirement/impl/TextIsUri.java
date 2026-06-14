package backend.academy.linktracker.bot.command.requirement.impl;

import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.requirement.InputContext;
import org.springframework.stereotype.Service;

@Service
public class TextIsUri extends AbstractRequirement {
    @Override
    protected boolean isSatisfied(InputContext inputContext) {
        String regex = "^(http|https)://.*$";
        return inputContext.update().message().text().matches(regex);
    }

    @Override
    protected String getRequirementName() {
        return "Проверка на URI";
    }
}
