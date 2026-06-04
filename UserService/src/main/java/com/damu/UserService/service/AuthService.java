package com.damu.UserService.service;

import com.damu.UserService.model.AuthRequest;
import com.damu.UserService.model.AuthResponse;
import com.damu.UserService.model.ChangePasswordRequest;
import com.damu.UserService.model.ForgotPasswordRequest;
import com.damu.UserService.model.RefreshTokenRequest;
import com.damu.UserService.model.ResetPasswordRequest;
import com.damu.UserService.model.TokenRequestResponse;
import com.damu.UserService.model.VerifyEmailRequest;

public interface AuthService {

    AuthResponse register(AuthRequest request);

    AuthResponse login(AuthRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    TokenRequestResponse forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(long userId, ChangePasswordRequest request);

    TokenRequestResponse createEmailVerificationToken(long userId);

    void verifyEmail(VerifyEmailRequest request);
}
