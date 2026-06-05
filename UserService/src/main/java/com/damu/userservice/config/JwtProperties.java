package com.damu.userservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String issuer,
        String audience,
        String privateKey,
        String publicKey,
        long accessTokenTtlMinutes,
        long refreshTokenTtlDays
) {
}
