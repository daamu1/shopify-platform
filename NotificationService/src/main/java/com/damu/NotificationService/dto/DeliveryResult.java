package com.damu.NotificationService.dto;

import com.damu.NotificationService.model.NotificationStatus;

public record DeliveryResult(
        String provider,
        String providerMessageId,
        NotificationStatus status
) {
}
