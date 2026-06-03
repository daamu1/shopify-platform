package com.damu.UserService.model;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
    private String deviceId;
}
