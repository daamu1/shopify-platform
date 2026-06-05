package com.damu.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Map;

public record EventPublishRequest(
        @NotBlank String eventType,
        @NotBlank String userId,
        String eventId,
        Map<String, Object> data,
        Instant occurredAt
) {
}
