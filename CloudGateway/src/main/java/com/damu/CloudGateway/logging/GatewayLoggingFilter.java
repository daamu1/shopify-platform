package com.damu.CloudGateway.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Log4j2
public class GatewayLoggingFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String correlationId = Optional.ofNullable(request.getHeader(CORRELATION_ID_HEADER)).filter(value -> !value.isBlank()).orElse(UUID.randomUUID().toString());
        long startedAt = System.currentTimeMillis();

        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        MDC.put(CORRELATION_ID_KEY, correlationId);
        log.info("Gateway request started method={} path={} remoteAddress={}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException exception) {
            log.error("Gateway request failed method={} path={} durationMs={} error={}", request.getMethod(), request.getRequestURI(), System.currentTimeMillis() - startedAt, exception.getMessage(), exception);
            throw exception;
        } finally {
            log.info("Gateway request completed method={} path={} status={} durationMs={}", request.getMethod(), request.getRequestURI(), response.getStatus(), System.currentTimeMillis() - startedAt);
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
}
