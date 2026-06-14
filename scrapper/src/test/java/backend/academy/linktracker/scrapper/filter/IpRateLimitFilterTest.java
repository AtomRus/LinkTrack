package backend.academy.linktracker.scrapper.filter;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.properties.ResilienceProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class IpRateLimitFilterTest {

    @Test
    void shouldAllowRequestsWithinLimit() throws ServletException, IOException {
        ResilienceProperties properties = new ResilienceProperties();
        properties.getRateLimit().setCapacity(2);
        properties.getRateLimit().setRefillTokens(2);
        properties.getRateLimit().setRefillPeriodSeconds(60);
        properties.getRateLimit().setEnabled(true);
        IpRateLimitFilter filter = new IpRateLimitFilter(properties);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/list");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean reachedChain = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> reachedChain.set(true);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(reachedChain).isTrue();
    }

    @Test
    void shouldReturn429WhenLimitExceeded() throws ServletException, IOException {
        ResilienceProperties properties = new ResilienceProperties();
        properties.getRateLimit().setCapacity(1);
        properties.getRateLimit().setRefillTokens(1);
        properties.getRateLimit().setRefillPeriodSeconds(60);
        properties.getRateLimit().setEnabled(true);
        IpRateLimitFilter filter = new IpRateLimitFilter(properties);

        MockHttpServletRequest first = new MockHttpServletRequest("GET", "/list");
        first.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        filter.doFilter(first, firstResponse, (req, res) -> {});

        MockHttpServletRequest second = new MockHttpServletRequest("GET", "/list");
        second.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();

        filter.doFilter(second, secondResponse, (req, res) -> {});

        assertThat(secondResponse.getStatus()).isEqualTo(429);
    }

    @Test
    void shouldSkipRateLimitWhenDisabled() throws ServletException, IOException {
        ResilienceProperties properties = new ResilienceProperties();
        properties.getRateLimit().setEnabled(false);
        IpRateLimitFilter filter = new IpRateLimitFilter(properties);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/list");
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean reachedChain = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> reachedChain.set(true);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(reachedChain).isTrue();
    }
}
