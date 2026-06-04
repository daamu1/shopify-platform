package com.damu.NotificationService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DeliveryAttempt {
    private Integer attemptNumber;
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
    private String provider;
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    private Instant attemptedAt;
}
