package com.damu.UserService.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenRequestResponse {
    private String message;
    private String token;
}
