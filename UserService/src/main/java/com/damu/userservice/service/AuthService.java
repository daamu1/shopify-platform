package com.damu.userservice.service;

import com.damu.userservice.model.AuthRequest;
import com.damu.userservice.model.AuthResponse;
import com.damu.userservice.model.ChangePasswordRequest;
import com.damu.userservice.model.ForgotPasswordRequest;
import com.damu.userservice.model.RefreshTokenRequest;
import com.damu.userservice.model.ResetPasswordRequest;
import com.damu.userservice.model.TokenRequestResponse;
import com.damu.userservice.model.VerifyEmailRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface AuthService {

    @Validated(AuthRequest.Registration.class)
    AuthResponse register(@Valid @NotNull(message = "Request is required", groups = AuthRequest.Registration.class) AuthRequest request);

    @Validated(AuthRequest.Login.class)
    AuthResponse login(@Valid @NotNull(message = "Request is required", groups = AuthRequest.Login.class) AuthRequest request);

    AuthResponse refresh(@Valid @NotNull(message = "Request is required") RefreshTokenRequest request);

    void logout(@Valid @NotNull(message = "Request is required") RefreshTokenRequest request);

    TokenRequestResponse forgotPassword(@Valid @NotNull(message = "Request is required") ForgotPasswordRequest request);

    void resetPassword(@Valid @NotNull(message = "Request is required") ResetPasswordRequest request);

    void changePassword(long userId, @Valid @NotNull(message = "Request is required") ChangePasswordRequest request);

    TokenRequestResponse createEmailVerificationToken(long userId);

    void verifyEmail(@Valid @NotNull(message = "Request is required") VerifyEmailRequest request);
}
