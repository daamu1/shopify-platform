package com.damu.notificationservice.repository;

import com.damu.notificationservice.model.NotificationChannel;
import com.damu.notificationservice.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, String> {

    Optional<NotificationTemplate> findFirstByEventTypeAndChannelAndActiveTrueOrderByVersionDesc(String eventType, NotificationChannel channel);

    List<NotificationTemplate> findByEventTypeOrderByVersionDesc(String eventType);

    List<NotificationTemplate> findByEventTypeAndChannelOrderByVersionDesc(String eventType, NotificationChannel channel);
}
