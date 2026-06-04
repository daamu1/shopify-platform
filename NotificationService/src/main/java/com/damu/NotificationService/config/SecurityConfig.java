package com.damu.NotificationService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.CollectionUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(authorize -> authorize.requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll().requestMatchers("/internal/**").hasAnyAuthority("SERVICE", "ADMIN").requestMatchers("/api/admin/**").hasAuthority("ADMIN").anyRequest().authenticated()).oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))).build();
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${app.security.jwt.secret}") String secret) {
        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(secret), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> new JwtAuthenticationToken(jwt, authorities(jwt));
    }

    private List<SimpleGrantedAuthority> authorities(Jwt jwt) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        List<String> permissions = jwt.getClaimAsStringList("permissions");
        if (!CollectionUtils.isEmpty(permissions)) {
            permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        }
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (!CollectionUtils.isEmpty(roles)) {
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        }
        return authorities;
    }
}
