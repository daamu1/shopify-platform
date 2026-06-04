package com.damu.NotificationService.repository;

import com.damu.NotificationService.model.InAppNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InAppNotificationRepository extends JpaRepository<InAppNotification, String> {

    List<InAppNotification> findByUserIdOrderByCreatedAtDesc(String userId);
}
