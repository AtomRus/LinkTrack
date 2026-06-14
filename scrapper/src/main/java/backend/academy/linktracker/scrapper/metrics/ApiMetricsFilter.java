package backend.academy.linktracker.scrapper.metrics;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ApiMetricsFilter extends OncePerRequestFilter {

    private final OperationMetrics operationMetrics;

    public ApiMetricsFilter(OperationMetrics operationMetrics) {
        this.operationMetrics = operationMetrics;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        operationMetrics.incrementApiRequests("http_api");
        filterChain.doFilter(request, response);
    }
}
