package com.damu.NotificationService.config;

import com.damu.NotificationService.model.NotificationChannel;
import com.damu.NotificationService.model.NotificationTemplate;
import com.damu.NotificationService.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DefaultNotificationTemplateSeeder implements CommandLineRunner {

    private static final String USER_REGISTERED = "user_registered";
    private static final String USER_LOGGED_IN = "user_logged_in";
    private static final String PASSWORD_RESET_REQUESTED = "password_reset_requested";
    private static final String PASSWORD_RESET_COMPLETED = "password_reset_completed";
    private static final String PASSWORD_CHANGED = "password_changed";

    private final NotificationTemplateRepository repository;

    @Override
    public void run(String... args) {
        seedEmailVerificationTemplate();
        seedInAppRegistrationTemplate();
        seedLoginEmailTemplate();
        seedLoginInAppTemplate();
        seedPasswordResetRequestedEmailTemplate();
        seedPasswordResetRequestedInAppTemplate();
        seedPasswordResetCompletedEmailTemplate();
        seedPasswordResetCompletedInAppTemplate();
        seedPasswordChangedEmailTemplate();
        seedPasswordChangedInAppTemplate();
    }

    private void seedEmailVerificationTemplate() {
        if (repository.findFirstByEventTypeAndChannelAndActiveTrueOrderByVersionDesc(USER_REGISTERED, NotificationChannel.EMAIL).isPresent()) {
            return;
        }

        Instant now = Instant.now();
        repository.save(NotificationTemplate.builder()
                .eventType(USER_REGISTERED)
                .channel(NotificationChannel.EMAIL)
                .name("User registration email verification")
                .version(1)
                .active(true)
                .locale("en")
                .subjectTemplate("Verify your email address")
                .bodyTemplate("""
                        <p>Hi {{fullName}},</p>
                        <p>Use this verification link to confirm your email address:</p>
                        <p><a href="{{verificationUrl}}">Verify email</a></p>
                        <p>If your client cannot open the link, use this token:</p>
                        <p>{{verificationToken}}</p>
                        """)
                .requiredVariables(Set.of("email", "verificationToken", "verificationUrl"))
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private void seedInAppRegistrationTemplate() {
        if (repository.findFirstByEventTypeAndChannelAndActiveTrueOrderByVersionDesc(USER_REGISTERED, NotificationChannel.IN_APP).isPresent()) {
            return;
        }

        Instant now = Instant.now();
        repository.save(NotificationTemplate.builder()
                .eventType(USER_REGISTERED)
                .channel(NotificationChannel.IN_APP)
                .name("User registration in-app verification prompt")
                .version(1)
                .active(true)
                .locale("en")
                .subjectTemplate("Verify your email")
                .bodyTemplate("Please verify {{email}} to finish setting up your account.")
                .requiredVariables(Set.of("email"))
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private void seedLoginEmailTemplate() {
        if (repository.findFirstByEventTypeAndChannelAndActiveTrueOrderByVersionDesc(USER_LOGGED_IN, NotificationChannel.EMAIL).isPresent()) {
            return;
        }

        Instant now = Instant.now();
        repository.save(NotificationTemplate.builder()
                .eventType(USER_LOGGED_IN)
                .channel(NotificationChannel.EMAIL)
                .name("User login email alert")
                .version(1)
                .active(true)
                .locale("en")
                .subjectTemplate("New login to your account")
                .bodyTemplate("""
                        <p>Hi {{fullName}},</p>
                        <p>Your account was just logged in.</p>
                        <p>Time: {{loginTime}}</p>
                        <p>Device: {{deviceId}}</p>
                        <p>If this was not you, change your password immediately.</p>
                        """)
                .requiredVariables(Set.of("email", "loginTime"))
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private void seedLoginInAppTemplate() {
        if (repository.findFirstByEventTypeAndChannelAndActiveTrueOrderByVersionDesc(USER_LOGGED_IN, NotificationChannel.IN_APP).isPresent()) {
            return;
        }

        Instant now = Instant.now();
        repository.save(NotificationTemplate.builder()
                .eventType(USER_LOGGED_IN)
                .channel(NotificationChannel.IN_APP)
                .name("User login in-app alert")
                .version(1)
                .active(true)
                .locale("en")
                .subjectTemplate("New login")
                .bodyTemplate("Your account was logged in at {{loginTime}}.")
                .requiredVariables(Set.of("loginTime"))
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private void seedPasswordResetRequestedEmailTemplate() {
        if (repository.findFirstByEventTypeAndChannelAndActiveTrueOrderByVersionDesc(PASSWORD_RESET_REQUESTED, NotificationChannel.EMAIL).isPresent()) {
            return;
        }

        Instant now = Instant.now();
        repository.save(NotificationTemplate.builder()
                .eventType(PASSWORD_RESET_REQUESTED)
                .channel(NotificationChannel.EMAIL)
                .name("Password reset request email")
                .version(1)
                .active(true)
                .locale("en")
                .subjectTemplate("Reset your password")
                .bodyTemplate("""
                        <p>Hi {{fullName}},</p>
                        <p>Use this link to reset your password:</p>
                        <p><a href="{{resetUrl}}">Reset password</a></p>
                        <p>This link expires in {{expiresInMinutes}} minutes.</p>
                        <p>If your client cannot open the link, use this token:</p>
                        <p>{{resetToken}}</p>
                        """)
                .requiredVariables(Set.of("email", "resetToken", "resetUrl", "expiresInMinutes"))
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private void seedPasswordResetRequestedInAppTemplate() {
        if (repository.findFirstByEventTypeAndChannelAndActiveTrueOrderByVersionDesc(PASSWORD_RESET_REQUESTED, NotificationChannel.IN_APP).isPresent()) {
            return;
        }

        Instant now = Instant.now();
        repository.save(NotificationTemplate.builder()
                .eventType(PASSWORD_RESET_REQUESTED)
                .channel(NotificationChannel.IN_APP)
                .name("Password reset request in-app alert")
                .version(1)
                .active(true)
                .locale("en")
                .subjectTemplate("Password reset requested")
                .bodyTemplate("A password reset was requested for {{email}}.")
                .requiredVariables(Set.of("email"))
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private void seedPasswordResetCompletedEmailTemplate() {
        if (repository.findFirstByEventTypeAndChannelAndActiveTrueOrderByVersionDesc(PASSWORD_RESET_COMPLETED, NotificationChannel.EMAIL).isPresent()) {
            return;
        }

        Instant now = Instant.now();
        repository.save(NotificationTemplate.builder()
                .eventType(PASSWORD_RESET_COMPLETED)
                .channel(NotificationChannel.EMAIL)
                .name("Password reset completed email")
                .version(1)
                .active(true)
                .locale("en")
                .subjectTemplate("Your password was reset")
                .bodyTemplate("""
                        <p>Hi {{fullName}},</p>
                        <p>Your password was reset at {{changedAt}}.</p>
                        <p>If this was not you, contact support immediately.</p>
                        """)
                .requiredVariables(Set.of("email", "changedAt"))
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private void seedPasswordResetCompletedInAppTemplate() {
        if (repository.findFirstByEventTypeAndChannelAndActiveTrueOrderByVersionDesc(PASSWORD_RESET_COMPLETED, NotificationChannel.IN_APP).isPresent()) {
            return;
        }

        Instant now = Instant.now();
        repository.save(NotificationTemplate.builder()
                .eventType(PASSWORD_RESET_COMPLETED)
                .channel(NotificationChannel.IN_APP)
                .name("Password reset completed in-app alert")
                .version(1)
                .active(true)
                .locale("en")
                .subjectTemplate("Password reset")
                .bodyTemplate("Your password was reset at {{changedAt}}.")
                .requiredVariables(Set.of("changedAt"))
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private void seedPasswordChangedEmailTemplate() {
        if (repository.findFirstByEventTypeAndChannelAndActiveTrueOrderByVersionDesc(PASSWORD_CHANGED, NotificationChannel.EMAIL).isPresent()) {
            return;
        }

        Instant now = Instant.now();
        repository.save(NotificationTemplate.builder()
                .eventType(PASSWORD_CHANGED)
                .channel(NotificationChannel.EMAIL)
                .name("Password changed email")
                .version(1)
                .active(true)
                .locale("en")
                .subjectTemplate("Your password was changed")
                .bodyTemplate("""
                        <p>Hi {{fullName}},</p>
                        <p>Your password was changed at {{changedAt}}.</p>
                        <p>If this was not you, contact support immediately.</p>
                        """)
                .requiredVariables(Set.of("email", "changedAt"))
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private void seedPasswordChangedInAppTemplate() {
        if (repository.findFirstByEventTypeAndChannelAndActiveTrueOrderByVersionDesc(PASSWORD_CHANGED, NotificationChannel.IN_APP).isPresent()) {
            return;
        }

        Instant now = Instant.now();
        repository.save(NotificationTemplate.builder()
                .eventType(PASSWORD_CHANGED)
                .channel(NotificationChannel.IN_APP)
                .name("Password changed in-app alert")
                .version(1)
                .active(true)
                .locale("en")
                .subjectTemplate("Password changed")
                .bodyTemplate("Your password was changed at {{changedAt}}.")
                .requiredVariables(Set.of("changedAt"))
                .createdAt(now)
                .updatedAt(now)
                .build());
    }
}
