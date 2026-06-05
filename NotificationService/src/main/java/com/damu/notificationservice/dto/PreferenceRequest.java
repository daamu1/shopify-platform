package com.damu.notificationservice.dto;

import com.damu.notificationservice.model.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record PreferenceRequest(
        @NotBlank String userId,
        @NotBlank String eventType,
        @NotEmpty Map<NotificationChannel, Boolean> channels
) {
}
