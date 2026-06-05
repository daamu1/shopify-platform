package com.damu.CloudGateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.CollectionUtils;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableWebSecurity
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
            "/user/v3/api-docs",
            "/notification/v3/api-docs"
    };

    private static final String[] PUBLIC_PATHS = {
            "/auth/**",
            "/user/auth/**"
    };

    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.securityMatcher(concat())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests.anyRequest().permitAll())
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(this::jwtAuthenticationToken)))
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${app.security.jwt.public-key}") String publicKey,
            @Value("${app.security.jwt.issuer}") String issuer,
            @Value("${app.security.jwt.audience}") String audience) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        RSAPublicKey rsaPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
        jwtDecoder.setJwtValidator(tokenValidator(issuer, audience));
        return jwtDecoder;
    }

    private AbstractAuthenticationToken jwtAuthenticationToken(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, authorities(jwt));
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

    private OAuth2TokenValidator<Jwt> tokenValidator(String issuer, String audience) {
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Missing required audience", null));
        return new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator);
    }

    private String[] concat() {
        String[] result = new String[GatewaySecurityConfig.DOCUMENTATION_PATHS.length + GatewaySecurityConfig.PUBLIC_PATHS.length];
        System.arraycopy(GatewaySecurityConfig.DOCUMENTATION_PATHS, 0, result, 0, GatewaySecurityConfig.DOCUMENTATION_PATHS.length);
        System.arraycopy(GatewaySecurityConfig.PUBLIC_PATHS, 0, result, GatewaySecurityConfig.DOCUMENTATION_PATHS.length, GatewaySecurityConfig.PUBLIC_PATHS.length);
        return result;
    }
}
