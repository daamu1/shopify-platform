package com.damu.NotificationService.dto;

import com.damu.NotificationService.model.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record PreferenceRequest(
        @NotBlank String userId,
        @NotBlank String eventType,
        @NotEmpty Map<NotificationChannel, Boolean> channels
) {
}
