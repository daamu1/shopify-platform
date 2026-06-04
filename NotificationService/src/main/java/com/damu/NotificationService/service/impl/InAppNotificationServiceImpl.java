package com.damu.NotificationService.service.impl;

import com.damu.NotificationService.exception.ResourceNotFoundException;
import com.damu.NotificationService.model.InAppNotification;
import com.damu.NotificationService.repository.InAppNotificationRepository;
import com.damu.NotificationService.service.InAppNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InAppNotificationServiceImpl implements InAppNotificationService {

    private final InAppNotificationRepository repository;

    @Override
    public List<InAppNotification> findByUser(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public InAppNotification markRead(String id) {
        InAppNotification notification = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("In-app notification not found"));
        notification.setRead(true);
        notification.setReadAt(Instant.now());
        return repository.save(notification);
    }
}
