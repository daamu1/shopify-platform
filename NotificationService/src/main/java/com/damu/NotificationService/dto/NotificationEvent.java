package com.damu.NotificationService.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Map;

public record NotificationEvent(
        @NotBlank String eventId,
        @NotBlank String eventType,
        @NotBlank String userId,
        Map<String, Object> data,
        Instant occurredAt
) {
}
