package com.damu.notificationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_logs", uniqueConstraints = @UniqueConstraint(name = "uk_notification_idempotency", columnNames = {"event_id", "user_id", "channel"}))
public class NotificationLog {
    @Id
    private String id;
    @Column(name = "event_id", nullable = false)
    private String eventId;
    private String eventType;
    @Column(name = "user_id", nullable = false)
    private String userId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;
    private String recipient;
    private String provider;
    private String providerMessageId;
    @Column(columnDefinition = "TEXT")
    private String subject;
    @Column(columnDefinition = "TEXT")
    private String body;
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    private Integer attemptCount;
    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> payload;
    @ElementCollection
    @CollectionTable(name = "notification_log_attempts", joinColumns = @JoinColumn(name = "notification_log_id"))
    private List<DeliveryAttempt> attempts = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
    private Instant sentAt;
    private Instant deliveredAt;

    @PrePersist
    void prePersist() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
    }
}
