package com.aiworkspace.shared.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Populates MDC with request-scoped context before the Spring Security chain runs,
 * so that even failed auth attempts carry a requestId in logs.
 *
 * Keys written: requestId, httpMethod, httpPath
 * Micrometer Tracing writes traceId and spanId automatically.
 * MdcUserInterceptor writes userId after JWT auth completes.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class MdcRequestFilter extends OncePerRequestFilter {

    static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String requestId = Optional.ofNullable(request.getHeader(REQUEST_ID_HEADER))
                .filter(h -> !h.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());

        MDC.put("requestId", requestId);
        MDC.put("httpMethod", request.getMethod());
        MDC.put("httpPath", request.getRequestURI());

        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
