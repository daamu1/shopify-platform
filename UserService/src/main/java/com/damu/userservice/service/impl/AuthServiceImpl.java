package com.damu.userservice.service.impl;

import com.damu.userservice.config.JwtProperties;
import com.damu.userservice.entity.ApplicationUser;
import com.damu.userservice.entity.EmailVerificationToken;
import com.damu.userservice.entity.PasswordResetToken;
import com.damu.userservice.entity.RefreshToken;
import com.damu.userservice.exception.UserServiceException;
import com.damu.userservice.model.*;
import com.damu.userservice.repository.ApplicationUserRepository;
import com.damu.userservice.repository.EmailVerificationTokenRepository;
import com.damu.userservice.repository.PasswordResetTokenRepository;
import com.damu.userservice.repository.RefreshTokenRepository;
import com.damu.userservice.service.AuthService;
import com.damu.userservice.producer.NotificationEventPublisher;
import com.damu.userservice.utils.AuthConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ApplicationUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final NotificationEventPublisher notificationEventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.auth.email-verification-url}")
    private String emailVerificationUrl;

    @Value("${app.auth.password-reset-url}")
    private String passwordResetUrl;

    @Transactional
    @Override
    @Validated(AuthRequest.Registration.class)
    public AuthResponse register(AuthRequest request) {
        userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .ifPresent(user -> {throw new UserServiceException("Email is already registered", "EMAIL_ALREADY_REGISTERED", 409);});

        Instant now = Instant.now();
        ApplicationUser user = ApplicationUser.builder()
                .authProvider("LOCAL")
                .authSubject(normalizeEmail(request.getEmail()))
                .email(normalizeEmail(request.getEmail()))
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(AuthConstants.DEFAULT_ROLE)
                .enabled(true)
                .emailVerified(false)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .createdAt(now)
                .updatedAt(now)
                .build();

        ApplicationUser savedUser = userRepository.save(user);
        String verificationToken = createEmailVerificationToken(savedUser);
        publishUserRegisteredEvent(savedUser, verificationToken);

        return issueTokens(savedUser, request.getDeviceId());
    }

    @Transactional
    @Override
    @Validated(AuthRequest.Login.class)
    public AuthResponse login(AuthRequest request) {
        ApplicationUser user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new UserServiceException("Invalid email or password", "INVALID_CREDENTIALS", 401));

        if (isLocked(user)) {
            throw new UserServiceException("Account is temporarily locked", "ACCOUNT_LOCKED", 423);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            recordFailedLogin(user);
            throw new UserServiceException("Invalid email or password", "INVALID_CREDENTIALS", 401);
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        publishUserLoggedInEvent(user, request.getDeviceId());
        return issueTokens(user, request.getDeviceId());
    }

    @Transactional
    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UserServiceException("Invalid refresh token", "INVALID_REFRESH_TOKEN", 401));

        if (refreshToken.getRevokedAt() != null || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UserServiceException("Invalid refresh token", "INVALID_REFRESH_TOKEN", 401);
        }

        String newRefreshToken = generateRefreshToken();
        refreshToken.setRevokedAt(Instant.now());
        refreshToken.setReplacedByTokenHash(hashToken(newRefreshToken));
        refreshTokenRepository.save(refreshToken);

        return issueTokens(refreshToken.getUser(), request.getDeviceId(), newRefreshToken);
    }

    @Transactional
    @Override
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenHash(hashToken(request.getRefreshToken()))
                .ifPresent(refreshToken -> {
                    refreshToken.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(refreshToken);
                });
    }

    @Transactional
    @Override
    public TokenRequestResponse forgotPassword(ForgotPasswordRequest request) {
        ApplicationUser user = userRepository.findByEmail(normalizeEmail(request.getEmail())).orElse(null);
        if (user == null) {
            return TokenRequestResponse.builder()
                    .message("If the email exists, a password reset token has been issued")
                    .build();
        }

        String resetToken = generateOpaqueToken();
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(hashToken(resetToken))
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(AuthConstants.PASSWORD_RESET_TOKEN_MINUTES, ChronoUnit.MINUTES))
                .build());
        publishPasswordResetRequestedEvent(user, resetToken);

        return TokenRequestResponse.builder()
                .message("Password reset token issued")
                .token(resetToken)
                .build();
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(hashToken(request.getToken()))
                .orElseThrow(() -> new UserServiceException("Invalid password reset token", "INVALID_PASSWORD_RESET_TOKEN", 401));
        if (resetToken.getUsedAt() != null || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UserServiceException("Invalid password reset token", "INVALID_PASSWORD_RESET_TOKEN", 401);
        }

        ApplicationUser user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setLockedUntil(null);
        user.setUpdatedAt(Instant.now());
        resetToken.setUsedAt(Instant.now());

        userRepository.save(user);
        passwordResetTokenRepository.save(resetToken);
        publishPasswordResetCompletedEvent(user);
    }

    @Transactional
    @Override
    public void changePassword(long userId, ChangePasswordRequest request) {
        ApplicationUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserServiceException("User profile not found", "USER_NOT_FOUND", 404));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new UserServiceException("Invalid current password", "INVALID_CURRENT_PASSWORD", 401);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        publishPasswordChangedEvent(user);
    }

    @Transactional
    @Override
    public TokenRequestResponse createEmailVerificationToken(long userId) {
        ApplicationUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserServiceException("User profile not found", "USER_NOT_FOUND", 404));
        if (user.isEmailVerified()) {
            return TokenRequestResponse.builder()
                    .message("Email is already verified")
                    .build();
        }

        String verificationToken = generateOpaqueToken();
        saveEmailVerificationToken(user, verificationToken);

        return TokenRequestResponse.builder()
                .message("Email verification token issued")
                .token(verificationToken)
                .build();
    }

    @Transactional
    @Override
    public void verifyEmail(VerifyEmailRequest request) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByTokenHash(hashToken(request.getToken()))
                .orElseThrow(() -> new UserServiceException("Invalid email verification token", "INVALID_EMAIL_VERIFICATION_TOKEN", 401));
        if (verificationToken.getVerifiedAt() != null || verificationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UserServiceException("Invalid email verification token", "INVALID_EMAIL_VERIFICATION_TOKEN", 401);
        }

        ApplicationUser user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setUpdatedAt(Instant.now());
        verificationToken.setVerifiedAt(Instant.now());

        userRepository.save(user);
        emailVerificationTokenRepository.save(verificationToken);
    }

    private AuthResponse issueTokens(ApplicationUser user, String deviceId) {
        return issueTokens(user, deviceId, generateRefreshToken());
    }

    private AuthResponse issueTokens(ApplicationUser user, String deviceId, String refreshTokenValue) {
        TokenIssue tokenIssue = jwtTokenService.issueAccessToken(user);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshTokenValue))
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(jwtProperties.refreshTokenTtlDays(), ChronoUnit.DAYS))
                .deviceId(deviceId)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(tokenIssue.token())
                .refreshToken(refreshTokenValue)
                .expiresAt(tokenIssue.expiresAt())
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .permissions(tokenIssue.permissions())
                .build();
    }

    private boolean isLocked(ApplicationUser user) {
        if (!user.isAccountLocked()) {
            return false;
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            return true;
        }
        user.setAccountLocked(false);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        return false;
    }

    private void recordFailedLogin(ApplicationUser user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        if (user.getFailedLoginAttempts() >= AuthConstants.MAX_FAILED_LOGIN_ATTEMPTS) {
            user.setAccountLocked(true);
            user.setLockedUntil(Instant.now().plus(AuthConstants.LOCK_MINUTES, ChronoUnit.MINUTES));
        }
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String createEmailVerificationToken(ApplicationUser user) {
        String verificationToken = generateOpaqueToken();
        saveEmailVerificationToken(user, verificationToken);
        return verificationToken;
    }

    private void saveEmailVerificationToken(ApplicationUser user, String verificationToken) {
        Instant now = Instant.now();
        emailVerificationTokenRepository.save(EmailVerificationToken.builder()
                .user(user)
                .tokenHash(hashToken(verificationToken))
                .createdAt(now)
                .expiresAt(now.plus(AuthConstants.EMAIL_VERIFICATION_TOKEN_HOURS, ChronoUnit.HOURS))
                .build());
    }

    private void publishUserRegisteredEvent(ApplicationUser user, String verificationToken) {
        notificationEventPublisher.publish(new NotificationEvent(
                "user_registered_%s_%s".formatted(user.getUserId(), UUID.randomUUID()),
                AuthConstants.USER_REGISTERED_EVENT,
                String.valueOf(user.getUserId()),
                Map.of(
                        "email", user.getEmail(),
                        "fullName", user.getFullName() == null ? "" : user.getFullName(),
                        "verificationToken", verificationToken,
                        "verificationUrl", buildVerificationUrl(verificationToken)
                ),
                Instant.now()
        ));
    }

    private void publishUserLoggedInEvent(ApplicationUser user, String deviceId) {
        notificationEventPublisher.publish(new NotificationEvent(
                "user_logged_in_%s_%s".formatted(user.getUserId(), UUID.randomUUID()),
                AuthConstants.USER_LOGGED_IN_EVENT,
                String.valueOf(user.getUserId()),
                Map.of(
                        "email", user.getEmail(),
                        "fullName", user.getFullName() == null ? "" : user.getFullName(),
                        "loginTime", Instant.now().toString(),
                        "deviceId", deviceId == null ? "" : deviceId
                ),
                Instant.now()
        ));
    }

    private void publishPasswordResetRequestedEvent(ApplicationUser user, String resetToken) {
        notificationEventPublisher.publish(new NotificationEvent(
                "password_reset_requested_%s_%s".formatted(user.getUserId(), UUID.randomUUID()),
                AuthConstants.PASSWORD_RESET_REQUESTED_EVENT,
                String.valueOf(user.getUserId()),
                Map.of(
                        "email", user.getEmail(),
                        "fullName", user.getFullName() == null ? "" : user.getFullName(),
                        "resetToken", resetToken,
                        "resetUrl", buildPasswordResetUrl(resetToken),
                        "expiresInMinutes", AuthConstants.PASSWORD_RESET_TOKEN_MINUTES
                ),
                Instant.now()
        ));
    }

    private void publishPasswordResetCompletedEvent(ApplicationUser user) {
        notificationEventPublisher.publish(new NotificationEvent(
                "password_reset_completed_%s_%s".formatted(user.getUserId(), UUID.randomUUID()),
                AuthConstants.PASSWORD_RESET_COMPLETED_EVENT,
                String.valueOf(user.getUserId()),
                Map.of(
                        "email", user.getEmail(),
                        "fullName", user.getFullName() == null ? "" : user.getFullName(),
                        "changedAt", Instant.now().toString()
                ),
                Instant.now()
        ));
    }

    private void publishPasswordChangedEvent(ApplicationUser user) {
        notificationEventPublisher.publish(new NotificationEvent(
                "password_changed_%s_%s".formatted(user.getUserId(), UUID.randomUUID()),
                AuthConstants.PASSWORD_CHANGED_EVENT,
                String.valueOf(user.getUserId()),
                Map.of(
                        "email", user.getEmail(),
                        "fullName", user.getFullName() == null ? "" : user.getFullName(),
                        "changedAt", Instant.now().toString()
                ),
                Instant.now()
        ));
    }

    private String buildVerificationUrl(String verificationToken) {
        String separator = emailVerificationUrl.contains("?") ? "&" : "?";
        return emailVerificationUrl + separator + "token=" + verificationToken;
    }

    private String buildPasswordResetUrl(String resetToken) {
        String separator = passwordResetUrl.contains("?") ? "&" : "?";
        return passwordResetUrl + separator + "token=" + resetToken;
    }

    private String generateRefreshToken() {
        return generateOpaqueToken();
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash token", exception);
        }
    }
}
