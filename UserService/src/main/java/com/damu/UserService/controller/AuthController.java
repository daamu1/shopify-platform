package com.damu.UserService.controller;

import com.damu.UserService.exception.UserServiceException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        log.info("Local user registration requested");
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        log.info("Local user login requested");
        return new ResponseEntity<>(authService.login(request), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return new ResponseEntity<>(authService.refresh(request), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<TokenRequestResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return new ResponseEntity<>(authService.forgotPassword(request), HttpStatus.ACCEPTED);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authenticatedUserId(jwt), request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/email-verification-token")
    public ResponseEntity<TokenRequestResponse> createEmailVerificationToken(@AuthenticationPrincipal Jwt jwt) {
        return new ResponseEntity<>(authService.createEmailVerificationToken(authenticatedUserId(jwt)), HttpStatus.CREATED);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private long authenticatedUserId(Jwt jwt) {
        if (jwt == null) {
            throw new UserServiceException("Authenticated user is required", "AUTHENTICATION_REQUIRED", 401);
        }
        return Long.parseLong(jwt.getSubject());
    }
}
