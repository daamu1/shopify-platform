package com.damu.notificationservice.repository;

import com.damu.notificationservice.model.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, String> {

    @Query("select distinct subscription from WebhookSubscription subscription join subscription.eventTypes eventType where subscription.active = true and eventType = :eventType")
    List<WebhookSubscription> findByActiveTrueAndEventTypesContaining(@Param("eventType") String eventType);
}
