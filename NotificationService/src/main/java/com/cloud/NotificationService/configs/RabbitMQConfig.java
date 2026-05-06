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

    public static final String EVENT_UPDATED_QUEUE = "notification.event.updated";
    public static final String EVENT_REMINDER_QUEUE = "notification.event.reminder";

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
    public Queue eventUpdatedQueue() {
        return QueueBuilder.durable(EVENT_UPDATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", DL_QUEUE)
                .build();
    }

    @Bean
    public Binding eventUpdatedBinding() {
        return BindingBuilder.bind(eventUpdatedQueue())
                .to(eventManagementExchange())
                .with("event.updated");
    }

    @Bean
    public Queue eventReminderQueue() {
        return QueueBuilder.durable(EVENT_REMINDER_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_NAME)
                .withArgument("x-dead-letter-routing-key", DL_QUEUE)
                .build();
    }

    @Bean
    public Binding eventReminderBinding() {
        return BindingBuilder.bind(eventReminderQueue())
                .to(eventManagementExchange())
                .with("event.reminder");
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
