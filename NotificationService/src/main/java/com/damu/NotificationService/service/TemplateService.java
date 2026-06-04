package com.damu.NotificationService.service;

import com.damu.NotificationService.dto.TemplateRequest;
import com.damu.NotificationService.model.NotificationChannel;
import com.damu.NotificationService.model.NotificationTemplate;

import java.util.List;

public interface TemplateService {

    NotificationTemplate create(TemplateRequest request);

    List<NotificationTemplate> findAll(String eventType, NotificationChannel channel);

    NotificationTemplate getById(String id);

    NotificationTemplate getActiveTemplate(String eventType, NotificationChannel channel);

    NotificationTemplate update(String id, TemplateRequest request);

    NotificationTemplate activate(String id);
}
