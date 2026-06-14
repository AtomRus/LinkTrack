package backend.academy.linktracker.bot.command.annotaion;

import backend.academy.linktracker.bot.command.telegrambot.UserState;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Command {
    String command();

    String description() default "";

    UserState[] states();
}
