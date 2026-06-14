package backend.academy.linktracker.scrapper.metrics;

import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class ApiMetricsFilterTest {

    @Mock
    private OperationMetrics operationMetrics;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private ApiMetricsFilter apiMetricsFilter;

    @Test
    void shouldCountHttpApiRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/links");
        MockHttpServletResponse response = new MockHttpServletResponse();

        apiMetricsFilter.doFilterInternal(request, response, filterChain);

        verify(operationMetrics).incrementApiRequests("http_api");
        verify(filterChain).doFilter(request, response);
    }
}
