package com.damu.NotificationService.dto;

import com.damu.NotificationService.model.NotificationChannel;

import java.util.Map;

public record SendNotificationCommand(
        String eventId,
        String eventType,
        String userId,
        NotificationChannel channel,
        String recipient,
        String subject,
        String body,
        Map<String, Object> data,
        int attemptNumber
) {
}
