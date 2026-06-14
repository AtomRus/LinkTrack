package backend.academy.linktracker.scrapper.filter;

import backend.academy.linktracker.scrapper.properties.ResilienceProperties;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class IpRateLimitFilter extends OncePerRequestFilter {
    private final ResilienceProperties resilienceProperties;
    private final Map<String, RateLimiter> limitersByIp = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ResilienceProperties.RateLimit rateLimit = resilienceProperties.getRateLimit();
        if (!rateLimit.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = resolveClientIp(request);
        RateLimiter rateLimiter = limitersByIp.computeIfAbsent(ip, ignored -> buildLimiter(rateLimit));
        if (!rateLimiter.acquirePermission()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private RateLimiter buildLimiter(ResilienceProperties.RateLimit rateLimit) {
        int limitForPeriod = (int) Math.min(rateLimit.getCapacity(), rateLimit.getRefillTokens());
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(limitForPeriod)
                .limitRefreshPeriod(Duration.ofSeconds(rateLimit.getRefillPeriodSeconds()))
                .timeoutDuration(Duration.ZERO)
                .build();
        return RateLimiter.of("ip-" + System.nanoTime() + "-" + rateLimit.getRefillTokens(), config);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
