package com.damu.NotificationService.service;

import com.damu.NotificationService.model.InAppNotification;

import java.util.List;

public interface InAppNotificationService {

    List<InAppNotification> findByUser(String userId);

    InAppNotification markRead(String id);
}
