package backend.academy.linktracker.bot.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.command.telegrambot.AbstractInputHandler;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandMetricsAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private AbstractInputHandler handler;

    @Test
    void aroundCommandShouldRecordMetrics() throws Throwable {
        BotMetrics botMetrics = new BotMetrics(new SimpleMeterRegistry());
        CommandMetricsAspect aspect = new CommandMetricsAspect(botMetrics);
        when(joinPoint.getTarget()).thenReturn(handler);
        when(handler.getHandlerName()).thenReturn("/track");
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.aroundCommand(joinPoint);

        assertThat(result).isEqualTo("ok");
        verify(joinPoint).proceed();
    }
}
