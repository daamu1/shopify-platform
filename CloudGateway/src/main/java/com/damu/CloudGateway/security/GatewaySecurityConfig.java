package com.damu.CloudGateway.security;

import com.damu.CloudGateway.model.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    private static final String[] DOCUMENTATION_PATHS = {
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-resources/**",
            "/order/v3/api-docs",
            "/payment/v3/api-docs",
            "/product/v3/api-docs",
            "/user/v3/api-docs"
    };

    private static final String[] PUBLIC_PATHS = {
            "/auth/**",
            "/user/auth/**"
    };

    @Bean
    @Order(1)
    public SecurityWebFilterChain publicSecurityFilterChain(ServerHttpSecurity http) {
        return http.securityMatcher(ServerWebExchangeMatchers.pathMatchers(concat(DOCUMENTATION_PATHS, PUBLIC_PATHS)))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorizeExchange -> authorizeExchange.anyExchange().permitAll())
                .build();
    }

    @Bean
    @Order(2)
    public SecurityWebFilterChain apiSecurityFilterChain(ServerHttpSecurity http, ObjectMapper objectMapper) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorizeExchange -> authorizeExchange.anyExchange().authenticated())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, exception) ->
                                writeErrorResponse(exchange, objectMapper, HttpStatus.UNAUTHORIZED,
                                        "Authentication is required", "AUTHENTICATION_REQUIRED"))
                        .accessDeniedHandler((exchange, exception) ->
                                writeErrorResponse(exchange, objectMapper, HttpStatus.FORBIDDEN,
                                        "Access denied", "ACCESS_DENIED")))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint((exchange, exception) ->
                                writeErrorResponse(exchange, objectMapper, HttpStatus.UNAUTHORIZED,
                                        "Invalid or missing token", "INVALID_TOKEN"))
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(@Value("${app.security.jwt.secret}") String secret) {
        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(secret), "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        return jwt -> Mono.just(new JwtAuthenticationToken(jwt, authorities(jwt)));
    }

    private List<SimpleGrantedAuthority> authorities(Jwt jwt) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        List<String> permissions = jwt.getClaimAsStringList("permissions");
        if (permissions != null) {
            permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        }
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        }
        return authorities;
    }

    private Mono<Void> writeErrorResponse(
            ServerWebExchange exchange,
            ObjectMapper objectMapper,
            HttpStatus status,
            String message,
            String errorCode) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(
                    ApiResponse.fail(message, status.value(), List.of(ApiResponse.ApiError.of(errorCode))));
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return exchange.getResponse().setComplete();
        }
    }

    private String[] concat(String[] first, String[] second) {
        String[] result = new String[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
