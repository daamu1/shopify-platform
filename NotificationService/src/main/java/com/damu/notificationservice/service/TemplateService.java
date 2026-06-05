package com.damu.notificationservice.service;

import com.damu.notificationservice.dto.TemplateRequest;
import com.damu.notificationservice.model.NotificationChannel;
import com.damu.notificationservice.model.NotificationTemplate;

import java.util.List;

public interface TemplateService {

    NotificationTemplate create(TemplateRequest request);

    List<NotificationTemplate> findAll(String eventType, NotificationChannel channel);

    NotificationTemplate getById(String id);

    NotificationTemplate getActiveTemplate(String eventType, NotificationChannel channel);

    NotificationTemplate update(String id, TemplateRequest request);

    NotificationTemplate activate(String id);
}
