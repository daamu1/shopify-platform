package com.damu.UserService.service;

import com.damu.UserService.model.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class NotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    public void publish(NotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event, message -> {
                message.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
                message.getMessageProperties().getHeaders().remove("__TypeId__");
                return message;
            });
        } catch (AmqpException exception) {
            log.warn("Unable to publish notification event eventId={} eventType={}",
                    event.eventId(), event.eventType(), exception);
        }
    }
}
