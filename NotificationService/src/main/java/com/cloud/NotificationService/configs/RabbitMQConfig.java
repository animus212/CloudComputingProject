package com.cloud.NotificationService.configs;

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

    public static final String USER_REGISTERED_QUEUE = "notification.user.registered";
    public static final String REG_CREATED_QUEUE = "notification.registration.created";
    public static final String REG_CANCELLED_QUEUE = "notification.registration.cancelled";

    public static final String DLX_NAME = "event.management.dlx";
    public static final String DL_QUEUE = "notification.dead.letter";

    @Bean
    public TopicExchange eventManagementExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DL_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DL_QUEUE);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(USER_REGISTERED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", DL_QUEUE)
                .build();
    }

    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder.bind(userRegisteredQueue())
                .to(eventManagementExchange())
                .with("user.registered");
    }

    @Bean
    public Queue registrationCreatedQueue() {
        return QueueBuilder.durable(REG_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", DL_QUEUE)
                .build();
    }

    @Bean
    public Binding registrationCreatedBinding() {
        return BindingBuilder.bind(registrationCreatedQueue())
                .to(eventManagementExchange())
                .with("registration.created");
    }

    @Bean
    public Queue registrationCancelledQueue() {
        return QueueBuilder.durable(REG_CANCELLED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", DL_QUEUE)
                .build();
    }

    @Bean
    public Binding registrationCancelledBinding() {
        return BindingBuilder.bind(registrationCancelledQueue())
                .to(eventManagementExchange())
                .with("registration.cancelled");
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
