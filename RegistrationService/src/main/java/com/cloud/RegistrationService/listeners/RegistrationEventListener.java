package com.cloud.RegistrationService.listeners;

import com.cloud.RegistrationService.events.EventUpdatedEvent;
import com.cloud.RegistrationService.events.UserDeletedEvent;
import com.cloud.RegistrationService.services.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RegistrationEventListener {
    private final RegistrationService registrationService;

    @RabbitListener(queues = "#{@userDeletedQueue.name}")
    public void handleUserDeleted(UserDeletedEvent event) {
        log.info("Received UserDeletedEvent for userId={}", event.getUserId());
        try {
            registrationService.cancelAllForUser(event.getUserId());
        } catch (Exception e) {
            log.error("Error processing UserDeletedEvent: {}", e.getMessage());
            throw e;
        }
    }

    @RabbitListener(queues = "#{@eventUpdatedQueue.name}")
    public void handleEventUpdated(EventUpdatedEvent event) throws InterruptedException {
        log.info("Received EventUpdatedEvent for eventId={}, status={}",
                event.getEventId(), event.getEventStatus());

        Thread.sleep(2000);
        if ("CANCELLED".equals(event.getEventStatus())) {
            try {
                registrationService.cancelAllForEvent(event.getEventId());
            } catch (Exception e) {
                log.error("Error processing EventUpdatedEvent cancellation: {}", e.getMessage());
                throw e;
            }
        }
    }
}