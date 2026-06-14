package backend.academy.linktracker.bot.command.annotaion;

import backend.academy.linktracker.bot.command.telegrambot.UserState;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TextHandler {
    String handlerName() default "";

    String description() default "";

    UserState[] states(); // Состояния, в которых обработчик может быть вызван
}
