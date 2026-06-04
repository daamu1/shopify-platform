package com.damu.UserService.service.impl;

import com.damu.UserService.config.JwtProperties;
import com.damu.UserService.entity.ApplicationUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public TokenIssue issueAccessToken(ApplicationUser user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.accessTokenTtlMinutes(), ChronoUnit.MINUTES);
        List<String> permissions = permissionsForRole(user.getRole());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .audience(List.of(jwtProperties.audience()))
                .subject(String.valueOf(user.getUserId()))
                .id(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("email", user.getEmail())
                .claim("role", roleAuthority(user.getRole()))
                .claim("roles", List.of(roleAuthority(user.getRole())))
                .claim("permissions", permissions)
                .claim("email_verified", user.isEmailVerified())
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
        )).getTokenValue();

        return new TokenIssue(token, expiresAt, permissions);
    }

    private String roleAuthority(String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return "Admin";
        }
        return "Customer";
    }

    private List<String> permissionsForRole(String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return List.of("product:read", "product:create", "product:update", "product:delete", "order:read:any", "user:manage");
        }
        return List.of("product:read", "order:create", "order:read:self", "payment:create", "user:read:self");
    }

    public record TokenIssue(String token, Instant expiresAt, List<String> permissions) {
    }
}
