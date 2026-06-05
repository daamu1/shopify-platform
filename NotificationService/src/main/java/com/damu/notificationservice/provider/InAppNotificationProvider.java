package com.damu.notificationservice.provider;

import com.damu.notificationservice.dto.DeliveryResult;
import com.damu.notificationservice.dto.SendNotificationCommand;
import com.damu.notificationservice.model.InAppNotification;
import com.damu.notificationservice.model.NotificationChannel;
import com.damu.notificationservice.model.NotificationStatus;
import com.damu.notificationservice.repository.InAppNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class InAppNotificationProvider implements NotificationProvider {

    private final InAppNotificationRepository repository;

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.IN_APP;
    }

    @Override
    public DeliveryResult send(SendNotificationCommand command) {
        InAppNotification notification = InAppNotification.builder()
                .userId(command.userId())
                .eventId(command.eventId())
                .eventType(command.eventType())
                .title(command.subject())
                .body(command.body())
                .data(command.data())
                .read(false)
                .createdAt(Instant.now())
                .build();
        InAppNotification saved = repository.save(notification);
        return new DeliveryResult("in-app", saved.getId(), NotificationStatus.SENT);
    }
}
