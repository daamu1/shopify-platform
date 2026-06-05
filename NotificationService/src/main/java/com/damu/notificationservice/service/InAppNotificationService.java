package com.damu.notificationservice.service;

import com.damu.notificationservice.model.InAppNotification;

import java.util.List;

public interface InAppNotificationService {

    List<InAppNotification> findByUser(String userId);

    InAppNotification markRead(String id);
}
