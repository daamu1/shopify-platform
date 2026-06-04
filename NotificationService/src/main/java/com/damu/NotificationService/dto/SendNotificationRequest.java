package com.damu.NotificationService.dto;

import com.damu.NotificationService.model.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;
import java.util.Set;

public record SendNotificationRequest(
        @NotBlank String eventId,
        @NotBlank String eventType,
        @NotBlank String userId,
        @NotEmpty Set<NotificationChannel> channels,
        Map<String, String> recipients,
        Map<String, Object> data
) {
}
