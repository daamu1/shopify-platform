package com.damu.UserService.controller;

import com.damu.UserService.model.UserProfileResponse;
import com.damu.UserService.model.UserRegistrationRequest;
import com.damu.UserService.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<UserProfileResponse> register(@AuthenticationPrincipal Jwt jwt, @RequestBody UserRegistrationRequest request) {
        if (!jwt.getSubject().equals(request.getAuthSubject())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authenticated subject does not match registration subject");
        }
        log.info("User registration API called authSubject={} email={}", request.getAuthSubject(), request.getEmail());
        return new ResponseEntity<>(userService.registerOrGetUser(request), HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return new ResponseEntity<>(userService.getUserById(Long.parseLong(jwt.getSubject())), HttpStatus.OK);
    }
}
