package com.damu.notificationservice.service;

import com.damu.notificationservice.dto.DeliveryResult;
import com.damu.notificationservice.dto.SendNotificationCommand;
import com.damu.notificationservice.model.NotificationLog;

import java.util.List;

public interface DeliveryLogService {

    NotificationLog createPending(SendNotificationCommand command);

    NotificationLog markSent(SendNotificationCommand command, DeliveryResult result);

    NotificationLog markFailed(SendNotificationCommand command, Throwable error);

    List<NotificationLog> findByEventId(String eventId);

    List<NotificationLog> findByUserId(String userId);
}
