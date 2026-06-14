package backend.academy.linktracker.bot.metrics;

import backend.academy.linktracker.bot.command.telegrambot.AbstractInputHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CommandMetricsAspect {

    private final BotMetrics botMetrics;

    public CommandMetricsAspect(BotMetrics botMetrics) {
        this.botMetrics = botMetrics;
    }

    @Around("execution(* backend.academy.linktracker.bot.command.telegrambot.AbstractInputHandler.handle(..))")
    public Object aroundCommand(ProceedingJoinPoint joinPoint) throws Throwable {
        AbstractInputHandler handler = (AbstractInputHandler) joinPoint.getTarget();
        botMetrics.incrementCommand(handler.getHandlerName());
        return botMetrics.record(BotMetrics.SCOPE_TELEGRAM_COMMAND, handler.getHandlerName(), () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable ex) {
                if (ex instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                if (ex instanceof Error error) {
                    throw error;
                }
                throw new IllegalStateException(ex);
            }
        });
    }
}
