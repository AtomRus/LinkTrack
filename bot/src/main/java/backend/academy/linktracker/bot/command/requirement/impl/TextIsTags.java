package backend.academy.linktracker.bot.command.requirement.impl;

import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.requirement.InputContext;
import org.springframework.stereotype.Service;

@Service
public class TextIsTags extends AbstractRequirement {
    @Override
    protected boolean isSatisfied(InputContext inputContext) {
        String input = inputContext.update().message().text();
        if (input == null || input.isBlank()) return false;

        if (input.length() > 500) return false;

        String[] tags = input.split(",");
        String tagRegex = "^[а-яА-ЯёЁa-zA-Z]+$";

        for (String tag : tags) {
            if (!tag.trim().matches(tagRegex)) return false;
        }
        return true;
    }

    @Override
    protected String getRequirementName() {
        return "Проврека строки на теги";
    }
}
