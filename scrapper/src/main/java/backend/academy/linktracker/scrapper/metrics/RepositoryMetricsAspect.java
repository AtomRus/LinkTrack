package backend.academy.linktracker.scrapper.metrics;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RepositoryMetricsAspect {

    private final OperationMetrics operationMetrics;

    public RepositoryMetricsAspect(OperationMetrics operationMetrics) {
        this.operationMetrics = operationMetrics;
    }

    @Around("execution(* backend.academy.linktracker.scrapper.repository..*(..))")
    public Object recordRepositoryCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String scopeType = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "."
                + joinPoint.getSignature().getName();
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long durationMs = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            operationMetrics.recordDuration(OperationMetrics.SCOPE_DATABASE, scopeType, durationMs);
        }
    }
}
