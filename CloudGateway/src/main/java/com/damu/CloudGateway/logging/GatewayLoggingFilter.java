package com.damu.CloudGateway.logging;

import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Configuration
@Log4j2
public class GatewayLoggingFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_KEY = "correlationId";

    @Bean
    public GlobalFilter requestLoggingGlobalFilter() {
        return (exchange, chain) -> {
            String correlationId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER))
                    .filter(value -> !value.isBlank())
                    .orElse(UUID.randomUUID().toString());
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header(CORRELATION_ID_HEADER, correlationId)
                    .build();
            long startedAt = System.currentTimeMillis();

            exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);
            MDC.put(CORRELATION_ID_KEY, correlationId);
            log.info("Gateway request started method={} path={} remoteAddress={}", request.getMethod(), request.getURI().getPath(), request.getRemoteAddress());
            MDC.remove(CORRELATION_ID_KEY);

            return chain.filter(exchange.mutate().request(request).build())
                    .doOnError(error -> {
                        MDC.put(CORRELATION_ID_KEY, correlationId);
                        log.error("Gateway request failed method={} path={} durationMs={} error={}", request.getMethod(), request.getURI().getPath(), System.currentTimeMillis() - startedAt, error.getMessage(), error);
                        MDC.remove(CORRELATION_ID_KEY);
                    })
                    .then(Mono.fromRunnable(() -> {
                        MDC.put(CORRELATION_ID_KEY, correlationId);
                        log.info("Gateway request completed method={} path={} status={} durationMs={}",
                                request.getMethod(), request.getURI().getPath(),
                                exchange.getResponse().getStatusCode(),
                                System.currentTimeMillis() - startedAt);
                        MDC.remove(CORRELATION_ID_KEY);
                    }));
        };
    }
}
