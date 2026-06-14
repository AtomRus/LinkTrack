package backend.academy.linktracker.bot.command.requirement.impl;

import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.requirement.InputContext;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class TextNotEmpty extends AbstractRequirement {

    @Override
    public boolean isSatisfied(InputContext inputContext) {
        Update update = inputContext.update();
        return update.message() != null && update.message().text() != null;
    }

    @Override
    protected String getRequirementName() {
        return "Проверка текста на null";
    }
}
