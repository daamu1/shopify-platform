package com.damu.notificationservice.dto;

import com.damu.notificationservice.model.NotificationChannel;
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
