package com.damu.NotificationService.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange notificationExchange(@Value("${app.rabbitmq.exchange}") String exchange) {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public Queue notificationEventsQueue(@Value("${app.rabbitmq.events-queue}") String queue, @Value("${app.rabbitmq.exchange}") String exchange, @Value("${app.rabbitmq.dead-routing-key}") String deadRoutingKey) {
        return QueueBuilder.durable(queue).withArgument("x-dead-letter-exchange", exchange).withArgument("x-dead-letter-routing-key", deadRoutingKey).build();
    }

    @Bean
    public Queue notificationDeadLetterQueue(@Value("${app.rabbitmq.dead-letter-queue}") String queue) {
        return QueueBuilder.durable(queue).build();
    }

    @Bean
    public Binding notificationEventsBinding(Queue notificationEventsQueue, DirectExchange notificationExchange, @Value("${app.rabbitmq.routing-key}") String routingKey) {
        return BindingBuilder.bind(notificationEventsQueue).to(notificationExchange).with(routingKey);
    }

    @Bean
    public Binding notificationDeadLetterBinding(Queue notificationDeadLetterQueue, DirectExchange notificationExchange, @Value("${app.rabbitmq.dead-routing-key}") String routingKey) {
        return BindingBuilder.bind(notificationDeadLetterQueue).to(notificationExchange).with(routingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(8);
        return factory;
    }
}
