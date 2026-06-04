package com.damu.NotificationService.provider;

import com.damu.NotificationService.dto.DeliveryResult;
import com.damu.NotificationService.dto.SendNotificationCommand;
import com.damu.NotificationService.model.NotificationChannel;

public interface NotificationProvider {

    boolean supports(NotificationChannel channel);

    DeliveryResult send(SendNotificationCommand command);
}
