package backend.academy.linktracker.bot.command.requirement.impl;

import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.requirement.InputContext;

public class TextIsTag extends AbstractRequirement {
    @Override
    protected boolean isSatisfied(InputContext inputContext) {
        String tagRegex = "^[\\wЁ-я#]+$";
        return inputContext.update().message().text().matches(tagRegex);
    }

    @Override
    protected String getRequirementName() {
        return "Проврека тега на валидность";
    }
}
