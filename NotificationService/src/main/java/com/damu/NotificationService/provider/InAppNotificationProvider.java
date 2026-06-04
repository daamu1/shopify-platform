package com.damu.NotificationService.provider;

import com.damu.NotificationService.dto.DeliveryResult;
import com.damu.NotificationService.dto.SendNotificationCommand;
import com.damu.NotificationService.model.InAppNotification;
import com.damu.NotificationService.model.NotificationChannel;
import com.damu.NotificationService.model.NotificationStatus;
import com.damu.NotificationService.repository.InAppNotificationRepository;
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
