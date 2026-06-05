package com.damu.userservice.model;

import java.time.Instant;
import java.util.List;

public record TokenIssue(String token, Instant expiresAt, List<String> permissions) {
    }