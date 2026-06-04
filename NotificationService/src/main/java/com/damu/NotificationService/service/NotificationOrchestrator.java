package com.damu.NotificationService.service;

import com.damu.NotificationService.dto.DeliveryResult;
import com.damu.NotificationService.dto.NotificationEvent;
import com.damu.NotificationService.dto.SendNotificationRequest;
import com.damu.NotificationService.model.NotificationStatus;

public interface NotificationOrchestrator {

    void process(NotificationEvent event);

    void sendDirect(SendNotificationRequest request);

    DeliveryResult markDelivered(String eventId, String providerMessageId);
}
