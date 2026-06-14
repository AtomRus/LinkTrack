package backend.academy.linktracker.scrapper.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class ResilienceExecutor {

    public <T> T execute(Supplier<T> supplier, Retry retry, CircuitBreaker circuitBreaker) {
        Supplier<T> withCircuitBreaker = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
        Supplier<T> withRetry = Retry.decorateSupplier(retry, withCircuitBreaker);
        return withRetry.get();
    }

    public void execute(Runnable runnable, Retry retry, CircuitBreaker circuitBreaker) {
        execute(
                () -> {
                    runnable.run();
                    return null;
                },
                retry,
                circuitBreaker);
    }
}
