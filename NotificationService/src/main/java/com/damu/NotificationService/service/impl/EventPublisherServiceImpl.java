package com.damu.NotificationService.service.impl;

import com.damu.NotificationService.dto.EventPublishRequest;
import com.damu.NotificationService.dto.NotificationEvent;
import com.damu.NotificationService.service.EventPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventPublisherServiceImpl implements EventPublisherService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    @Override
    public NotificationEvent publish(EventPublishRequest request) {
        NotificationEvent event = new NotificationEvent(
                request.eventId() == null || request.eventId().isBlank() ? UUID.randomUUID().toString() : request.eventId(),
                request.eventType(),
                request.userId(),
                request.data(),
                request.occurredAt() == null ? Instant.now() : request.occurredAt());

        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        return event;
    }
}
