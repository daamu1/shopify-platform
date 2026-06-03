package com.damu.UserService.controller;

import com.damu.UserService.exception.UserServiceException;
import com.damu.UserService.model.ApiResponse;
import com.damu.UserService.model.AuthRequest;
import com.damu.UserService.model.AuthResponse;
import com.damu.UserService.model.ChangePasswordRequest;
import com.damu.UserService.model.ForgotPasswordRequest;
import com.damu.UserService.model.RefreshTokenRequest;
import com.damu.UserService.model.ResetPasswordRequest;
import com.damu.UserService.model.TokenRequestResponse;
import com.damu.UserService.model.VerifyEmailRequest;
import com.damu.UserService.service.impl.AuthService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Log4j2
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@RequestBody AuthRequest request) {
        log.info("Local user registration requested");
        return ApiResponse.ok("User registered successfully", HttpStatus.CREATED.value(), authService.register(request));
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<AuthResponse> login(@RequestBody AuthRequest request) {
        log.info("Local user login requested");
        return ApiResponse.ok("Login successful", HttpStatus.OK.value(), authService.login(request));
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok("Token refreshed successfully", HttpStatus.OK.value(), authService.refresh(request));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ApiResponse.ok("Logout successful", HttpStatus.OK.value());
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<TokenRequestResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ApiResponse.ok("Password reset token created", HttpStatus.ACCEPTED.value(), authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok("Password reset successfully", HttpStatus.OK.value());
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authenticatedUserId(jwt), request);
        return ApiResponse.ok("Password changed successfully", HttpStatus.OK.value());
    }

    @PostMapping("/email-verification-token")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TokenRequestResponse> createEmailVerificationToken(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok("Email verification token created", HttpStatus.CREATED.value(),
                authService.createEmailVerificationToken(authenticatedUserId(jwt)));
    }

    @PostMapping("/verify-email")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> verifyEmail(@RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ApiResponse.ok("Email verified successfully", HttpStatus.OK.value());
    }

    private long authenticatedUserId(Jwt jwt) {
        if (jwt == null) {
            throw new UserServiceException("Authenticated user is required", "AUTHENTICATION_REQUIRED", 401);
        }
        return Long.parseLong(jwt.getSubject());
    }
}
