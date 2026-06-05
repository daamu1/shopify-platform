package com.damu.notificationservice.service.impl;

import com.damu.notificationservice.dto.PreferenceRequest;
import com.damu.notificationservice.model.NotificationChannel;
import com.damu.notificationservice.model.UserPreference;
import com.damu.notificationservice.repository.UserPreferenceRepository;
import com.damu.notificationservice.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PreferenceServiceImpl implements PreferenceService {

    private static final List<NotificationChannel> DEFAULT_CHANNELS = List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP);

    private final UserPreferenceRepository repository;

    @Override
    public UserPreference upsert(PreferenceRequest request) {
        Instant now = Instant.now();
        UserPreference preference = repository.findByUserIdAndEventType(request.userId(), request.eventType())
                .orElseGet(() -> UserPreference.builder()
                        .userId(request.userId())
                        .eventType(request.eventType())
                        .createdAt(now)
                        .build());
        preference.setChannels(new EnumMap<>(request.channels()));
        preference.setUpdatedAt(now);
        return repository.save(preference);
    }

    @Override
    public List<UserPreference> findByUser(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public List<NotificationChannel> getEnabledChannels(String userId, String eventType) {
        return repository.findByUserIdAndEventType(userId, eventType)
                .map(this::enabledChannels)
                .orElse(DEFAULT_CHANNELS);
    }

    private List<NotificationChannel> enabledChannels(UserPreference preference) {
        Map<NotificationChannel, Boolean> channels = preference.getChannels();
        if (channels == null || channels.isEmpty()) {
            return DEFAULT_CHANNELS;
        }
        return channels.entrySet().stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();
    }
}
