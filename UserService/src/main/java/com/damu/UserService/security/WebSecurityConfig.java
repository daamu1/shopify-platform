package com.damu.UserService.security;

import com.damu.UserService.model.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private static final String[] DOCUMENTATION_PATHS = {
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**"
    };

    private static final String[] PUBLIC_PATHS = {
            "/auth/register",
            "/auth/login",
            "/auth/refresh",
            "/auth/logout",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/auth/verify-email"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter, ObjectMapper objectMapper) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequest -> authorizeRequest
                        .requestMatchers(DOCUMENTATION_PATHS).permitAll()
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> writeErrorResponse(response, objectMapper, HttpStatus.UNAUTHORIZED, "Authentication is required", "AUTHENTICATION_REQUIRED"))
                        .accessDeniedHandler((request, response, accessDeniedException) -> writeErrorResponse(response, objectMapper, HttpStatus.FORBIDDEN, "Access denied", "ACCESS_DENIED")))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint((request, response, authException) -> writeErrorResponse(response, objectMapper, HttpStatus.UNAUTHORIZED, "Invalid or missing token", "INVALID_TOKEN")));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    private void writeErrorResponse(HttpServletResponse response, ObjectMapper objectMapper, HttpStatus status, String message, String errorCode) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(message, status.value(), List.of(ApiResponse.ApiError.of(errorCode))));
    }
}
