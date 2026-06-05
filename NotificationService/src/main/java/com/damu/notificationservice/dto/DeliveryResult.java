package com.damu.notificationservice.dto;

import com.damu.notificationservice.model.NotificationStatus;

public record DeliveryResult(
        String provider,
        String providerMessageId,
        NotificationStatus status
) {
}
