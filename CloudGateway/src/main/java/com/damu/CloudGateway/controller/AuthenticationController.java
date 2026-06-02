package com.damu.CloudGateway.controller;

import com.damu.CloudGateway.model.AuthenticationResponse;
import com.damu.CloudGateway.model.UserProfileResponse;
import com.damu.CloudGateway.model.UserRegistrationRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@Log4j2
public class AuthenticationController {

    private final WebClient.Builder webClientBuilder;

    public AuthenticationController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping({"/authenticate/login", "/authentication/login"})
    public ResponseEntity<AuthenticationResponse> login(@AuthenticationPrincipal OidcUser oidcUser, @RegisteredOAuth2AuthorizedClient("auth0") OAuth2AuthorizedClient client) {
        log.info("Authentication login request received user={}", oidcUser.getEmail());
        UserProfileResponse userProfile = registerUserProfile(oidcUser, client);
        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .userId(oidcUser.getEmail())
                .accessToken(client.getAccessToken().getTokenValue())
                .refreshToken(client.getRefreshToken() == null ? null : client.getRefreshToken().getTokenValue())
                .expiresAt(Objects.requireNonNull(client.getAccessToken().getExpiresAt()).getEpochSecond())
                .authorityList(oidcUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .userProfile(userProfile)
                .build();
        log.info("Authentication login completed user={} authorities={}", oidcUser.getEmail(), authenticationResponse.getAuthorityList());
        return new ResponseEntity<>(authenticationResponse, HttpStatus.OK);
    }

    private UserProfileResponse registerUserProfile(OidcUser oidcUser, OAuth2AuthorizedClient client) {
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .authProvider("AUTH0")
                .authSubject(oidcUser.getSubject())
                .email(oidcUser.getEmail())
                .fullName(oidcUser.getFullName())
                .build();

        return webClientBuilder.build()
                .post()
                .uri("lb://USER-SERVICE/user/register")
                .headers(headers -> headers.setBearerAuth(client.getAccessToken().getTokenValue()))
                .bodyValue(registrationRequest)
                .retrieve()
                .bodyToMono(UserProfileResponse.class)
                .block();
    }
}
