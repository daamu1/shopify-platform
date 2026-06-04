package com.damu.NotificationService.consumer;

import com.damu.NotificationService.dto.NotificationEvent;
import com.damu.NotificationService.exception.NotificationProcessingException;
import com.damu.NotificationService.service.NotificationOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class NotificationEventConsumer {

    private final NotificationOrchestrator notificationOrchestrator;

    @RabbitListener(queues = "${app.rabbitmq.events-queue}")
    public void consume(NotificationEvent event) {
        try {
            log.info("Notification event received eventId={} eventType={} userId={}", event.eventId(), event.eventType(), event.userId());
            notificationOrchestrator.process(event);
        } catch (Exception exception) {
            throw new NotificationProcessingException("Failed to process notification event " + event.eventId(), exception);
        }
    }
}
