package com.cloud.RegistrationService.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "event.management.exchange";

    // Queue names
    public static final String USER_DELETED_QUEUE   = "registration.user.deleted";
    public static final String EVENT_UPDATED_QUEUE  = "registration.event.updated";

    @Bean
    public TopicExchange eventManagementExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    // Bind to user.deleted routing key
    @Bean
    public Queue userDeletedQueue() {
        return QueueBuilder.durable(USER_DELETED_QUEUE).build();
    }

    @Bean
    public Binding userDeletedBinding() {
        return BindingBuilder.bind(userDeletedQueue())
                .to(eventManagementExchange())
                .with("user.deleted");
    }

    // Bind to event.updated routing key
    @Bean
    public Queue eventUpdatedQueue() {
        return QueueBuilder.durable(EVENT_UPDATED_QUEUE).build();
    }

    @Bean
    public Binding eventUpdatedBinding() {
        return BindingBuilder.bind(eventUpdatedQueue())
                .to(eventManagementExchange())
                .with("event.updated");
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