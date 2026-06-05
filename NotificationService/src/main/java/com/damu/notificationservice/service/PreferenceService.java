package com.damu.notificationservice.service;

import com.damu.notificationservice.dto.PreferenceRequest;
import com.damu.notificationservice.model.NotificationChannel;
import com.damu.notificationservice.model.UserPreference;

import java.util.List;

public interface PreferenceService {

    UserPreference upsert(PreferenceRequest request);

    List<UserPreference> findByUser(String userId);

    List<NotificationChannel> getEnabledChannels(String userId, String eventType);
}
