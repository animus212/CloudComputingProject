package com.cloud.NotificationService.listeners;

import com.cloud.NotificationService.events.EventReminderEvent;
import com.cloud.NotificationService.events.EventUpdatedEvent;
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

    @RabbitListener(queues = "#{@eventUpdatedQueue.name}")
    public void handleEventUpdated(EventUpdatedEvent event) {
        log.info("Received EventUpdatedEvent for event: {}", event.getEventId());

        try {
            notificationService.createEventUpdatedNotification(event);
        } catch (Exception e) {
            log.error("Error processing EventUpdatedEvent: {}", e.getMessage());

            throw e;
        }
    }

    @RabbitListener(queues = "#{@eventReminderQueue.name}")
    public void handleEventReminder(EventReminderEvent event) {
        log.info("Received EventReminderEvent for event: {}", event.getEventId());

        try {
            notificationService.createEventReminderNotification(event);
        } catch (Exception e) {
            log.error("Error processing EventReminderEvent: {}", e.getMessage());

            throw e;
        }
    }
}
