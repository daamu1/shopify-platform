package com.damu.userservice.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private Instant expiresAt;
    private long userId;
    private String email;
    private String role;
    private List<String> permissions;
}
