package com.damu.NotificationService.provider;

import com.damu.NotificationService.dto.DeliveryResult;
import com.damu.NotificationService.dto.SendNotificationCommand;
import com.damu.NotificationService.model.NotificationChannel;
import com.damu.NotificationService.model.NotificationStatus;
import com.damu.NotificationService.model.WebhookSubscription;
import com.damu.NotificationService.repository.WebhookSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebhookNotificationProvider implements NotificationProvider {

    @Qualifier("webhookRestClient")
    private final RestClient webhookRestClient;
    private final WebhookSubscriptionRepository subscriptionRepository;

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.WEBHOOK;
    }

    @Override
    public DeliveryResult send(SendNotificationCommand command) {
        for (WebhookSubscription subscription : subscriptionRepository.findByActiveTrueAndEventTypesContaining(command.eventType())) {
            webhookRestClient.post()
                    .uri(subscription.getTargetUrl())
                    .body(Map.of(
                            "eventId", command.eventId(),
                            "eventType", command.eventType(),
                            "userId", command.userId(),
                            "data", command.data() == null ? Map.of() : command.data()
                    ))
                    .retrieve()
                    .toBodilessEntity();
        }
        return new DeliveryResult("webhook", command.eventId(), NotificationStatus.SENT);
    }
}
