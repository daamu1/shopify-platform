package com.damu.notificationservice.provider;

import com.damu.notificationservice.dto.DeliveryResult;
import com.damu.notificationservice.dto.SendNotificationCommand;
import com.damu.notificationservice.model.NotificationChannel;

public interface NotificationProvider {

    boolean supports(NotificationChannel channel);

    DeliveryResult send(SendNotificationCommand command);
}
