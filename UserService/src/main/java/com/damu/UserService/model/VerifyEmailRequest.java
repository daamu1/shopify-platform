package com.damu.UserService.model;

import lombok.Data;

@Data
public class VerifyEmailRequest {
    private String token;
}
