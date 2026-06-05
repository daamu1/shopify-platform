package com.damu.notificationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "webhook_subscriptions")
public class WebhookSubscription {
    @Id
    private String id;
    @Column(nullable = false)
    private String ownerId;
    @Column(nullable = false)
    private String targetUrl;
    @ElementCollection
    @CollectionTable(name = "webhook_subscription_events", joinColumns = @JoinColumn(name = "webhook_subscription_id"))
    @Column(name = "event_type")
    private Set<String> eventTypes;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
    }
}
