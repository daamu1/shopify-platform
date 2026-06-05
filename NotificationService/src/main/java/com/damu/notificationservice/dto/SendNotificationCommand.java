package com.damu.notificationservice.dto;

import com.damu.notificationservice.model.NotificationChannel;

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
