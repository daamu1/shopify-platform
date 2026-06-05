package com.damu.notificationservice.repository;

import com.damu.notificationservice.model.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, String> {

    Optional<UserPreference> findByUserIdAndEventType(String userId, String eventType);

    List<UserPreference> findByUserId(String userId);
}
