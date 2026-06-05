package com.damu.notificationservice.service;

import com.damu.notificationservice.dto.DeliveryResult;
import com.damu.notificationservice.dto.NotificationEvent;
import com.damu.notificationservice.dto.SendNotificationRequest;

public interface NotificationOrchestrator {

    void process(NotificationEvent event);

    void sendDirect(SendNotificationRequest request);

    DeliveryResult markDelivered(String eventId, String providerMessageId);
}
