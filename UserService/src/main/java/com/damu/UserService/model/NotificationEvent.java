package com.damu.UserService.model;

import java.time.Instant;
import java.util.Map;

public record NotificationEvent(
        String eventId,
        String eventType,
        String userId,
        Map<String, Object> data,
        Instant occurredAt
) {
}
