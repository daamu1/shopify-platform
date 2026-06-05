package com.damu.userservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    @Size(max = 255, message = "Refresh token cannot exceed 255 characters")
    private String refreshToken;

    @NotBlank(message = "Device ID is required")
    @Size(max = 120, message = "Device ID cannot exceed 120 characters")
    private String deviceId;
}
