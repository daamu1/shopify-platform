package com.damu.CloudGateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
@EnableWebFluxSecurity
public class OktaOAuth2WebSecurity {

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

    private static final String[] LOGIN_PATHS = {
            "/authenticate/login",
            "/authentication/login",
            "/login",
            "/login/**",
            "/oauth2/**"
    };

    @Bean
    @Order(1)
    public SecurityWebFilterChain documentationSecurityFilterChain(ServerHttpSecurity http) {
        return http.securityMatcher(ServerWebExchangeMatchers.pathMatchers(DOCUMENTATION_PATHS))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorizeExchange -> authorizeExchange
                        .anyExchange().permitAll())
                .build();
    }

    @Bean
    @Order(2)
    public SecurityWebFilterChain oauthLoginSecurityFilterChain(ServerHttpSecurity http) {
        return http.securityMatcher(ServerWebExchangeMatchers.pathMatchers(LOGIN_PATHS))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorizeExchange -> authorizeExchange
                        .pathMatchers("/login", "/login/**", "/oauth2/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2Login(oauth2Login -> {
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                        .jwt(jwt -> {
                        }))
                .build();
    }

    @Bean
    @Order(3)
    public SecurityWebFilterChain apiSecurityFilterChain(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorizeExchange -> authorizeExchange
                        .anyExchange().authenticated())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, exception) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                        .jwt(jwt -> {
                        }))
                .build();
    }
}
