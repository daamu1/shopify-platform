package com.damu.userservice.controller;

import com.damu.userservice.exception.UserServiceException;
import com.damu.userservice.model.ApiResponse;
import com.damu.userservice.model.UserProfileResponse;
import com.damu.userservice.model.UserRegistrationRequest;
import com.damu.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserProfileResponse> register(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid UserRegistrationRequest request) {
        if (!jwt.getSubject().equals(request.getAuthSubject())) {
            throw new UserServiceException("Authenticated subject does not match registration subject", "FORBIDDEN", HttpStatus.FORBIDDEN.value());
        }
        log.info("User registration API called authSubject={} email={}", request.getAuthSubject(), request.getEmail());
        return ApiResponse.ok("User profile ready", HttpStatus.OK.value(), userService.registerOrGetUser(request));
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserProfileResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok("User profile fetched successfully", HttpStatus.OK.value(), userService.getUserById(Long.parseLong(jwt.getSubject())));
    }
}
