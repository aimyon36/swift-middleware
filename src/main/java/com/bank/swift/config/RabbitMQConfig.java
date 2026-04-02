package com.bank.swift.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置
 */
@Configuration
public class RabbitMQConfig {

    @Value("${swift.notification.mq.exchange}")
    private String exchangeName;

    @Value("${swift.notification.mq.queues.raw}")
    private String rawQueue;

    @Value("${swift.notification.mq.queues.parsed}")
    private String parsedQueue;

    @Value("${swift.notification.mq.queues.notified}")
    private String notifiedQueue;

    @Value("${swift.notification.mq.routing-keys.raw}")
    private String rawRoutingKey;

    @Value("${swift.notification.mq.routing-keys.parsed}")
    private String parsedRoutingKey;

    @Value("${swift.notification.mq.routing-keys.notified}")
    private String notifiedRoutingKey;

    @Bean
    public TopicExchange swiftExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue rawQueue() {
        return QueueBuilder.durable(rawQueue).build();
    }

    @Bean
    public Queue parsedQueue() {
        return QueueBuilder.durable(parsedQueue).build();
    }

    @Bean
    public Queue notifiedQueue() {
        return QueueBuilder.durable(notifiedQueue).build();
    }

    @Bean
    public Binding rawBinding(Queue rawQueue, TopicExchange swiftExchange) {
        return BindingBuilder.bind(rawQueue).to(swiftExchange).with(rawRoutingKey);
    }

    @Bean
    public Binding parsedBinding(Queue parsedQueue, TopicExchange swiftExchange) {
        return BindingBuilder.bind(parsedQueue).to(swiftExchange).with(parsedRoutingKey);
    }

    @Bean
    public Binding notifiedBinding(Queue notifiedQueue, TopicExchange swiftExchange) {
        return BindingBuilder.bind(notifiedQueue).to(swiftExchange).with(notifiedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
