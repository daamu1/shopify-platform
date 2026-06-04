package com.damu.NotificationService.service;

import com.damu.NotificationService.dto.DeliveryResult;
import com.damu.NotificationService.dto.SendNotificationCommand;
import com.damu.NotificationService.model.NotificationLog;

import java.util.List;

public interface DeliveryLogService {

    NotificationLog createPending(SendNotificationCommand command);

    NotificationLog markSent(SendNotificationCommand command, DeliveryResult result);

    NotificationLog markFailed(SendNotificationCommand command, Throwable error);

    List<NotificationLog> findByEventId(String eventId);

    List<NotificationLog> findByUserId(String userId);
}
