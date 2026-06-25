package com.spring_midterm.midterm.web.filter;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    private final LoggingFilter filter = new LoggingFilter();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    void shouldSetRequestIdFromHeader() throws Exception {
        when(request.getHeader("X-Request-Id")).thenReturn("custom-id");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("X-Request-Id", "custom-id");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldGenerateRequestIdWhenHeaderMissing() throws Exception {
        when(request.getHeader("X-Request-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(eq("X-Request-Id"), matches("[a-z0-9]{12}"));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldClearMdcAfterRequest() throws Exception {
        when(request.getHeader("X-Request-Id")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");

        filter.doFilterInternal(request, response, filterChain);

        assert MDC.get("requestId") == null;
        assert MDC.get("username") == null;
    }
}
