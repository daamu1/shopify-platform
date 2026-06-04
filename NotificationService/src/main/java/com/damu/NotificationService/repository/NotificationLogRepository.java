package com.damu.NotificationService.repository;

import com.damu.NotificationService.model.NotificationChannel;
import com.damu.NotificationService.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, String> {

    Optional<NotificationLog> findByEventIdAndUserIdAndChannel(String eventId, String userId, NotificationChannel channel);

    List<NotificationLog> findByEventId(String eventId);

    List<NotificationLog> findByUserIdOrderByCreatedAtDesc(String userId);
}
