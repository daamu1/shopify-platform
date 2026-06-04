package com.damu.NotificationService.provider;

import com.damu.NotificationService.dto.DeliveryResult;
import com.damu.NotificationService.dto.SendNotificationCommand;
import com.damu.NotificationService.model.NotificationChannel;
import com.damu.NotificationService.model.NotificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SendGridEmailProvider implements NotificationProvider {

    private final RestClient sendGridRestClient;

    @Value("${app.notification.email.from}")
    private String fromEmail;

    @Value("${app.notification.sendgrid.api-key}")
    private String apiKey;

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }

    @Override
    public DeliveryResult send(SendNotificationCommand command) {
        if (apiKey == null || apiKey.isBlank()) {
            return new DeliveryResult("sendgrid-disabled", null, NotificationStatus.SENT);
        }

        Map<String, Object> request = Map.of(
                "personalizations", List.of(Map.of(
                        "to", List.of(Map.of("email", command.recipient()))
                )),
                "from", Map.of("email", fromEmail),
                "subject", command.subject(),
                "content", List.of(Map.of(
                        "type", "text/html",
                        "value", command.body()
                ))
        );

        sendGridRestClient.post()
                .uri("/v3/mail/send")
                .body(request)
                .retrieve()
                .toBodilessEntity();

        return new DeliveryResult("sendgrid", null, NotificationStatus.SENT);
    }
}
