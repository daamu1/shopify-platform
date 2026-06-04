package com.damu.UserService.service.impl;

import com.damu.UserService.config.JwtProperties;
import com.damu.UserService.entity.ApplicationUser;
import com.damu.UserService.entity.EmailVerificationToken;
import com.damu.UserService.entity.PasswordResetToken;
import com.damu.UserService.entity.RefreshToken;
import com.damu.UserService.exception.UserServiceException;
import com.damu.UserService.model.AuthRequest;
import com.damu.UserService.model.AuthResponse;
import com.damu.UserService.model.ChangePasswordRequest;
import com.damu.UserService.model.ForgotPasswordRequest;
import com.damu.UserService.model.NotificationEvent;
import com.damu.UserService.model.RefreshTokenRequest;
import com.damu.UserService.model.ResetPasswordRequest;
import com.damu.UserService.model.TokenRequestResponse;
import com.damu.UserService.model.VerifyEmailRequest;
import com.damu.UserService.repository.ApplicationUserRepository;
import com.damu.UserService.repository.EmailVerificationTokenRepository;
import com.damu.UserService.repository.PasswordResetTokenRepository;
import com.damu.UserService.repository.RefreshTokenRepository;
import com.damu.UserService.service.AuthService;
import com.damu.UserService.service.NotificationEventPublisher;
import com.damu.UserService.util.AuthConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    public AuthServiceImpl(ApplicationUserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           EmailVerificationTokenRepository emailVerificationTokenRepository,
                           NotificationEventPublisher notificationEventPublisher,
                           PasswordEncoder passwordEncoder,
                           JwtTokenService jwtTokenService,
                           JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.notificationEventPublisher = notificationEventPublisher;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    @Override
    public AuthResponse register(AuthRequest request) {
        validateCredentialsRequest(request);
        userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .ifPresent(user -> {
                    throw new UserServiceException("Email is already registered", "EMAIL_ALREADY_REGISTERED", 409);
                });

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
    public AuthResponse login(AuthRequest request) {
        validateCredentialsRequest(request);
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
        if (request == null || !StringUtils.hasText(request.getRefreshToken())) {
            throw new UserServiceException("refreshToken is required", "INVALID_REFRESH_TOKEN", 400);
        }

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
        if (request == null || !StringUtils.hasText(request.getRefreshToken())) {
            return;
        }
        refreshTokenRepository.findByTokenHash(hashToken(request.getRefreshToken()))
                .ifPresent(refreshToken -> {
                    refreshToken.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(refreshToken);
                });
    }

    @Transactional
    @Override
    public TokenRequestResponse forgotPassword(ForgotPasswordRequest request) {
        if (request == null || !StringUtils.hasText(request.getEmail())) {
            throw new UserServiceException("email is required", "INVALID_PASSWORD_RESET_REQUEST", 400);
        }

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
        if (request == null || !StringUtils.hasText(request.getToken()) || !StringUtils.hasText(request.getNewPassword())) {
            throw new UserServiceException("token and newPassword are required", "INVALID_PASSWORD_RESET_REQUEST", 400);
        }
        validatePasswordStrength(request.getNewPassword());

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
        if (request == null || !StringUtils.hasText(request.getCurrentPassword()) || !StringUtils.hasText(request.getNewPassword())) {
            throw new UserServiceException("currentPassword and newPassword are required", "INVALID_CHANGE_PASSWORD_REQUEST", 400);
        }
        validatePasswordStrength(request.getNewPassword());

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
        if (request == null || !StringUtils.hasText(request.getToken())) {
            throw new UserServiceException("token is required", "INVALID_EMAIL_VERIFICATION_REQUEST", 400);
        }

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
        JwtTokenService.TokenIssue tokenIssue = jwtTokenService.issueAccessToken(user);
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

    private void validateCredentialsRequest(AuthRequest request) {
        if (request == null || !StringUtils.hasText(request.getEmail()) || !StringUtils.hasText(request.getPassword())) {
            throw new UserServiceException("email and password are required", "INVALID_AUTH_REQUEST", 400);
        }
        if (!isValidEmail(request.getEmail())) {
            throw new UserServiceException("email must be a valid email address", "INVALID_EMAIL", 400);
        }
        if (request.getPassword().length() < 8) {
            throw new UserServiceException("password must be at least 8 characters", "WEAK_PASSWORD", 400);
        }
    }

    private void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new UserServiceException("password must be at least 8 characters", "WEAK_PASSWORD", 400);
        }
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

    private boolean isValidEmail(String email) {
        return StringUtils.hasText(email) && AuthConstants.EMAIL_PATTERN.matcher(email.trim()).matches();
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
