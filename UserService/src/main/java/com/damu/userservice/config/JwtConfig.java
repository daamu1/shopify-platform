package com.damu.userservice.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public SecretKey iamJwtSecretKey(JwtProperties jwtProperties) {
        return new SecretKeySpec(Base64.getDecoder().decode(jwtProperties.secret()), "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey iamJwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(iamJwtSecretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey iamJwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(iamJwtSecretKey).build();
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> new JwtAuthenticationToken(jwt, authorities(jwt));
    }

    private List<SimpleGrantedAuthority> authorities(Jwt jwt) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        jwt.getClaimAsStringList("permissions").forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        jwt.getClaimAsStringList("roles").forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        return authorities;
    }
}
