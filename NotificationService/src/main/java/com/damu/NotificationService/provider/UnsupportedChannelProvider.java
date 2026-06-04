package com.damu.NotificationService.provider;

import com.damu.NotificationService.dto.DeliveryResult;
import com.damu.NotificationService.dto.SendNotificationCommand;
import com.damu.NotificationService.model.NotificationChannel;
import com.damu.NotificationService.model.NotificationStatus;
import org.springframework.stereotype.Component;

@Component
public class UnsupportedChannelProvider implements NotificationProvider {

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.SMS || channel == NotificationChannel.PUSH;
    }

    @Override
    public DeliveryResult send(SendNotificationCommand command) {
        return new DeliveryResult(command.channel().name().toLowerCase() + "-placeholder", null, NotificationStatus.SENT);
    }
}
