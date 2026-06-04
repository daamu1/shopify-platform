package com.damu.NotificationService.service.impl;

import com.damu.NotificationService.dto.DeliveryResult;
import com.damu.NotificationService.dto.NotificationEvent;
import com.damu.NotificationService.dto.SendNotificationCommand;
import com.damu.NotificationService.dto.SendNotificationRequest;
import com.damu.NotificationService.model.NotificationChannel;
import com.damu.NotificationService.model.NotificationLog;
import com.damu.NotificationService.model.NotificationStatus;
import com.damu.NotificationService.model.NotificationTemplate;
import com.damu.NotificationService.provider.NotificationProvider;
import com.damu.NotificationService.service.DeliveryLogService;
import com.damu.NotificationService.service.NotificationOrchestrator;
import com.damu.NotificationService.service.PreferenceService;
import com.damu.NotificationService.service.TemplateService;
import com.damu.NotificationService.template.TemplateRenderer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationOrchestratorImpl implements NotificationOrchestrator {

    private final PreferenceService preferenceService;
    private final TemplateService templateService;
    private final TemplateRenderer templateRenderer;
    private final DeliveryLogService deliveryLogService;
    private final List<NotificationProvider> providers;

    @Override
    public void process(NotificationEvent event) {
        for (NotificationChannel channel : preferenceService.getEnabledChannels(event.userId(), event.eventType())) {
            sendForChannel(event.eventId(), event.eventType(), event.userId(), channel, null, event.data());
        }
    }

    @Override
    public void sendDirect(SendNotificationRequest request) {
        for (NotificationChannel channel : request.channels()) {
            String recipient = request.recipients() == null ? null : request.recipients().get(channel.name());
            sendForChannel(request.eventId(), request.eventType(), request.userId(), channel, recipient, request.data());
        }
    }

    private void sendForChannel(String eventId, String eventType, String userId, NotificationChannel channel, String recipient,Map<String, Object> data) {
        NotificationTemplate template = templateService.getActiveTemplate(eventType, channel);
        String subject = templateRenderer.render(template.getSubjectTemplate(), data);
        String body = templateRenderer.render(template.getBodyTemplate(), data);
        SendNotificationCommand command = new SendNotificationCommand(
                eventId,
                eventType,
                userId,
                channel,
                resolveRecipient(channel, userId, recipient, data),
                subject,
                body,
                data,
                1);

        NotificationLog log = deliveryLogService.createPending(command);
        if (log.getStatus() != NotificationStatus.PENDING || log.getAttemptCount() != 0) {
            return;
        }

        try {
            DeliveryResult result = getProvider(channel).send(command);
            deliveryLogService.markSent(command, result);
        } catch (Exception exception) {
            deliveryLogService.markFailed(command, exception);
            throw exception;
        }
    }

    private String resolveRecipient(NotificationChannel channel, String userId, String recipient, Map<String, Object> data) {
        if (recipient != null && !recipient.isBlank()) {
            return recipient;
        }
        Object value = data == null ? null : data.get(channel.name().toLowerCase() + "Recipient");
        if (value == null && channel == NotificationChannel.EMAIL && data != null) {
            value = data.get("email");
        }
        if (value == null && channel == NotificationChannel.SMS && data != null) {
            value = data.get("phone");
        }
        return value == null ? userId : value.toString();
    }

    private NotificationProvider getProvider(NotificationChannel channel) {
        return providers.stream()
                .filter(provider -> provider.supports(channel))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No provider found for channel " + channel));
    }

    @Override
    public DeliveryResult markDelivered(String eventId, String providerMessageId) {
        return new DeliveryResult("callback", providerMessageId, NotificationStatus.DELIVERED);
    }
}
