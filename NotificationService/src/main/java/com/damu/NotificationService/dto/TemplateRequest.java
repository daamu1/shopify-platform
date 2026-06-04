package com.damu.NotificationService.dto;

import com.damu.NotificationService.model.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record TemplateRequest(
        @NotBlank String eventType,
        @NotNull NotificationChannel channel,
        @NotBlank String name,
        String locale,
        String subjectTemplate,
        @NotBlank String bodyTemplate,
        Set<String> requiredVariables,
        Boolean active
) {
}
