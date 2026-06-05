package com.damu.userservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyEmailRequest {

    @NotBlank(message = "Token is required")
    @Size(min = 6, max = 255, message = "Invalid token format")
    private String token;
}