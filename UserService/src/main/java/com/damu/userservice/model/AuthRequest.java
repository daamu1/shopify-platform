package com.damu.userservice.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRequest {

    public interface Registration {
    }

    public interface Login {
    }

    @NotBlank(message = "Email is required", groups = {Registration.class, Login.class})
    @Email(message = "Please provide a valid email address", groups = {Registration.class, Login.class})
    @Size(max = 150, message = "Email cannot exceed 150 characters", groups = {Registration.class, Login.class})
    private String email;

    @NotBlank(message = "Password is required", groups = {Registration.class, Login.class})
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters", groups = {Registration.class, Login.class})
    private String password;

    @NotBlank(message = "Full name is required", groups = Registration.class)
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters", groups = Registration.class)
    private String fullName;

    @NotBlank(message = "Device ID is required", groups = {Registration.class, Login.class})
    @Size(max = 120, message = "Device ID cannot exceed 120 characters", groups = {Registration.class, Login.class})
    private String deviceId;
}
