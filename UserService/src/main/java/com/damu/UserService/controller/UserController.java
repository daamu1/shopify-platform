package com.damu.UserService.controller;

import com.damu.UserService.exception.UserServiceException;
import com.damu.UserService.model.ApiResponse;
import com.damu.UserService.model.UserProfileResponse;
import com.damu.UserService.model.UserRegistrationRequest;
import com.damu.UserService.service.UserService;
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

@RestController
@RequestMapping("/user")
@Log4j2
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserProfileResponse> register(@AuthenticationPrincipal Jwt jwt, @RequestBody UserRegistrationRequest request) {
        if (!jwt.getSubject().equals(request.getAuthSubject())) {
            throw new UserServiceException("Authenticated subject does not match registration subject", "FORBIDDEN", HttpStatus.FORBIDDEN.value());
        }
        log.info("User registration API called authSubject={} email={}", request.getAuthSubject(), request.getEmail());
        return ApiResponse.ok("User profile ready", HttpStatus.OK.value(), userService.registerOrGetUser(request));
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserProfileResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok("User profile fetched successfully", HttpStatus.OK.value(),
                userService.getUserById(Long.parseLong(jwt.getSubject())));
    }
}
