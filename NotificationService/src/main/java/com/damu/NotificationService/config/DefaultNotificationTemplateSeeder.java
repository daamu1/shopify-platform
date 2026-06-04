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

    private final NotificationTemplateRepository repository;

    @Override
    public void run(String... args) {
        seedEmailVerificationTemplate();
        seedInAppRegistrationTemplate();
        seedLoginEmailTemplate();
        seedLoginInAppTemplate();
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
}
