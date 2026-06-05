package com.damu.notificationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "notification_templates")
public class NotificationTemplate {
    @Id
    private String id;
    @Column(nullable = false)
    private String eventType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;
    @Column(nullable = false)
    private String name;
    private Integer version;
    private Boolean active;
    private String locale;
    @Column(columnDefinition = "TEXT")
    private String subjectTemplate;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;
    @ElementCollection
    @CollectionTable(name = "notification_template_variables", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "variable_name")
    private Set<String> requiredVariables;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
    }
}
