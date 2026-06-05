package com.damu.userservice.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    @NotBlank(message = "Auth provider is required")
    @Size(max = 50, message = "Auth provider cannot exceed 50 characters")
    private String authProvider;

    @NotBlank(message = "Auth subject is required")
    @Size(max = 150, message = "Auth subject cannot exceed 150 characters")
    private String authSubject;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;
}
