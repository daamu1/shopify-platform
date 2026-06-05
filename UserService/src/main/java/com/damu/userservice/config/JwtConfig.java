package com.damu.userservice.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public RSAPrivateKey jwtPrivateKey(JwtProperties jwtProperties) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.privateKey());
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    @Bean
    public RSAPublicKey jwtPublicKey(JwtProperties jwtProperties) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.publicKey());
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAPublicKey jwtPublicKey, RSAPrivateKey jwtPrivateKey) {
        RSAKey rsaKey = new RSAKey.Builder(jwtPublicKey).privateKey(jwtPrivateKey).build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey jwtPublicKey, JwtProperties jwtProperties) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(jwtPublicKey).build();
        jwtDecoder.setJwtValidator(tokenValidator(jwtProperties.issuer(), jwtProperties.audience()));
        return jwtDecoder;
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

    private OAuth2TokenValidator<Jwt> tokenValidator(String issuer, String audience) {
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Missing required audience", null));
        return new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator);
    }
}
