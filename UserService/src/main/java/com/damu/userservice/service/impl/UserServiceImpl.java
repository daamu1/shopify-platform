package com.damu.userservice.service.impl;

import com.damu.userservice.entity.ApplicationUser;
import com.damu.userservice.exception.UserServiceException;
import com.damu.userservice.model.UserProfileResponse;
import com.damu.userservice.model.UserRegistrationRequest;
import com.damu.userservice.repository.ApplicationUserRepository;
import com.damu.userservice.service.UserService;
import com.damu.userservice.utils.AuthConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;

@Service
@Log4j2
@Validated
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ApplicationUserRepository applicationUserRepository;

    @Override
    @Transactional
    public UserProfileResponse registerOrGetUser(UserRegistrationRequest request) {
        log.info("Register user request received authProvider={} authSubject={} email={}", request.getAuthProvider(), request.getAuthSubject(), request.getEmail());

        ApplicationUser user = applicationUserRepository.findByAuthSubject(request.getAuthSubject())
                .map(existingUser -> updateExistingUser(existingUser, request))
                .orElseGet(() -> createUser(request));

        ApplicationUser savedUser = applicationUserRepository.save(user);
        log.info("Register user request completed userId={} authSubject={} role={}", savedUser.getUserId(), savedUser.getAuthSubject(), savedUser.getRole());
        return toResponse(savedUser);
    }

    @Override
    public UserProfileResponse getUserById(long userId) {
        ApplicationUser user = applicationUserRepository.findById(userId)
                .orElseThrow(() -> new UserServiceException("User profile not found", "USER_NOT_FOUND", 404));
        return toResponse(user);
    }

    private ApplicationUser createUser(UserRegistrationRequest request) {
        applicationUserRepository.findByEmail(request.getEmail())
                .ifPresent(existingUser -> {
                    throw new UserServiceException("Email is already registered with a different identity", "EMAIL_ALREADY_REGISTERED", 409);
                });

        Instant now = Instant.now();
        return ApplicationUser.builder()
                .authProvider(normalize(request.getAuthProvider(), "AUTH0"))
                .authSubject(request.getAuthSubject())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(AuthConstants.DEFAULT_ROLE)
                .enabled(true)
                .emailVerified(false)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private ApplicationUser updateExistingUser(ApplicationUser user, UserRegistrationRequest request) {
        user.setAuthProvider(normalize(request.getAuthProvider(), user.getAuthProvider()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setUpdatedAt(Instant.now());
        return user;
    }

    private String normalize(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase() : fallback;
    }

    private UserProfileResponse toResponse(ApplicationUser user) {
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .authProvider(user.getAuthProvider())
                .authSubject(user.getAuthSubject())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
