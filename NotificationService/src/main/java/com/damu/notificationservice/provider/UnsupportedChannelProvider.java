package com.damu.notificationservice.provider;

import com.damu.notificationservice.dto.DeliveryResult;
import com.damu.notificationservice.dto.SendNotificationCommand;
import com.damu.notificationservice.model.NotificationChannel;
import com.damu.notificationservice.model.NotificationStatus;
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
