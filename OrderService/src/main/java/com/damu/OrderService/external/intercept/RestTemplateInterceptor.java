package com.damu.OrderService.external.intercept;

import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public ClientHttpResponse intercept(@NonNull HttpRequest request, byte @NonNull [] body, @NonNull ClientHttpRequestExecution execution) throws IOException {
        String correlationId = MDC.get(CORRELATION_ID_KEY);
        if (correlationId != null && !correlationId.isBlank()) {
            request.getHeaders().add(CORRELATION_ID_HEADER, correlationId);
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String authorization = attributes.getRequest().getHeader(AUTHORIZATION_HEADER);
            if (authorization != null && !authorization.isBlank()) {
                request.getHeaders().set(AUTHORIZATION_HEADER, authorization);
            }
        }

        return execution.execute(request, body);
    }
}
