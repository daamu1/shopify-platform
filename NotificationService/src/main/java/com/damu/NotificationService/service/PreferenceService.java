package com.damu.NotificationService.service;

import com.damu.NotificationService.dto.PreferenceRequest;
import com.damu.NotificationService.model.NotificationChannel;
import com.damu.NotificationService.model.UserPreference;

import java.util.List;

public interface PreferenceService {

    UserPreference upsert(PreferenceRequest request);

    List<UserPreference> findByUser(String userId);

    List<NotificationChannel> getEnabledChannels(String userId, String eventType);
}
