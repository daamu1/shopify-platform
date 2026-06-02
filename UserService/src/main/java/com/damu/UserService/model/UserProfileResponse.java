package com.damu.UserService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private long userId;
    private String authProvider;
    private String authSubject;
    private String email;
    private String fullName;
    private String role;
    private Instant createdAt;
    private Instant updatedAt;
}
