package com.cloud.NotificationService.listeners;

import com.cloud.NotificationService.events.RegistrationCancelledEvent;
import com.cloud.NotificationService.events.RegistrationCreatedEvent;
import com.cloud.NotificationService.events.UserRegisteredEvent;
import com.cloud.NotificationService.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {
    private final NotificationService notificationService;

    @RabbitListener(queues = "#{@userRegisteredQueue.name}")
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for user: {}", event.getUserId());

        try {
            notificationService.sendWelcomeNotification(
                    event.getUserId(),
                    event.getEmail(),
                    event.getFirstName()
            );
        } catch (Exception e) {
            log.error("Error processing UserRegisteredEvent: {}", e.getMessage());

            throw e;
        }
    }

    @RabbitListener(queues = "#{@registrationCreatedQueue.name}")
    public void handleRegistrationCreated(RegistrationCreatedEvent event) {
        log.info("Received RegistrationCreatedEvent: reg={}", event.getRegistrationId());

        try {
            notificationService.sendRegistrationConfirmation(
                    event.getUserId(),
                    event.getUserEmail(),
                    event.getEventTitle(),
                    event.getRegisteredAt()
            );
        } catch (Exception e) {
            log.error("Error processing RegistrationCreatedEvent: {}", e.getMessage());

            throw e;
        }
    }

    @RabbitListener(queues = "#{@registrationCancelledQueue.name}")
    public void handleRegistrationCancelled(RegistrationCancelledEvent event) {
        log.info("Received RegistrationCancelledEvent: reg={}", event.getRegistrationId());

        try {
            notificationService.sendCancellationNotification(
                    event.getUserId(),
                    event.getUserEmail(),
                    event.getEventTitle()
            );
        } catch (Exception e) {
            log.error("Error processing RegistrationCancelledEvent: {}", e.getMessage());

            throw e;
        }
    }
}
