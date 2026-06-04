package com.damu.NotificationService.service;

import com.damu.NotificationService.dto.EventPublishRequest;
import com.damu.NotificationService.dto.NotificationEvent;

public interface EventPublisherService {

    NotificationEvent publish(EventPublishRequest request);
}
