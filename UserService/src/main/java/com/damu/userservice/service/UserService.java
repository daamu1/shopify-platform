package com.damu.userservice.service;

import com.damu.userservice.model.UserProfileResponse;
import com.damu.userservice.model.UserRegistrationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface UserService {
    UserProfileResponse registerOrGetUser(@Valid @NotNull(message = "Registration request is required") UserRegistrationRequest request);

    UserProfileResponse getUserById(long userId);
}
