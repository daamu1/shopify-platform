package com.damu.NotificationService.repository;

import com.damu.NotificationService.model.NotificationChannel;
import com.damu.NotificationService.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, String> {

    Optional<NotificationTemplate> findFirstByEventTypeAndChannelAndActiveTrueOrderByVersionDesc(String eventType, NotificationChannel channel);

    List<NotificationTemplate> findByEventTypeOrderByVersionDesc(String eventType);

    List<NotificationTemplate> findByEventTypeAndChannelOrderByVersionDesc(String eventType, NotificationChannel channel);
}
