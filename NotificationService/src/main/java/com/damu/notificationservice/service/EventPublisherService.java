package com.damu.notificationservice.service;

import com.damu.notificationservice.dto.EventPublishRequest;
import com.damu.notificationservice.dto.NotificationEvent;

public interface EventPublisherService {

    NotificationEvent publish(EventPublishRequest request);
}
