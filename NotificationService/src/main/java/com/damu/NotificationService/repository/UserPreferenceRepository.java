package com.damu.NotificationService.repository;

import com.damu.NotificationService.model.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, String> {

    Optional<UserPreference> findByUserIdAndEventType(String userId, String eventType);

    List<UserPreference> findByUserId(String userId);
}
