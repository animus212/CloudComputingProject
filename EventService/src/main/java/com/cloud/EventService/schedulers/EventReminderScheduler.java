package com.cloud.EventService.schedulers;

import com.cloud.EventService.entities.Event;
import com.cloud.EventService.events.EventReminderEvent;
import com.cloud.EventService.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static com.cloud.EventService.configs.RabbitMQConfig.EVENT_REMINDER_ROUTING_KEY;
import static com.cloud.EventService.configs.RabbitMQConfig.EXCHANGE_NAME;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventReminderScheduler {
    private final EventRepository eventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedRate = 86_400_000)
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = now.plusDays(1);

        List<Event> events = eventRepository.findByStartDateBetween(now, reminderTime);

        for (Event event : events) {
            EventReminderEvent reminderEvent = new EventReminderEvent(
                    event.getId(),
                    event.getTitle(),
                    event.getStartDate()
            );

            rabbitTemplate.convertAndSend(
                    EXCHANGE_NAME,
                    EVENT_REMINDER_ROUTING_KEY,
                    reminderEvent
            );
        }
    }
}
