package com.cloud.NotificationService.services;

import com.cloud.NotificationService.clients.RegistrationServiceClient;
import com.cloud.NotificationService.dtos.responses.NotificationResponse;
import com.cloud.NotificationService.entities.Notification;
import com.cloud.NotificationService.entities.NotificationType;
import com.cloud.NotificationService.events.EventReminderEvent;
import com.cloud.NotificationService.events.EventUpdatedEvent;
import com.cloud.NotificationService.repositories.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final RegistrationServiceClient registrationServiceClient;

    @Transactional
    public void createEventUpdatedNotification(EventUpdatedEvent event) {
        String message = String.format("""
                        The event you registered for has been updated.
                        
                        Event: %s
                        Date: %s
                        Location: %s
                        Event Type: %s
                        Event Status: %s
                        
                        Please check the event page for the latest information.
                        """,
                event.getTitle(),
                event.getStartDate(),
                event.getLocation(),
                event.getEventType(),
                event.getEventStatus()
        );

        createNotifications(event.getEventId(), message, NotificationType.EVENT_UPDATED);
    }

    @Transactional
    public void createEventReminderNotification(EventReminderEvent event) {
        String message = String.format("""
                        Your event is starting soon:

                        Event: %s
                        Date: %s

                        Don't miss it!
                        """,
                event.getTitle(),
                event.getStartDate()
        );

        createNotifications(event.getEventId(), message, NotificationType.EVENT_REMINDER);
    }

    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse).toList();
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = findAndAssertOwner(notificationId, userId);

        notification.setRead(true);

        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(userId);

        unread.forEach(n -> n.setRead(true));

        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        notificationRepository.delete(findAndAssertOwner(notificationId, userId));
    }

    @Transactional
    public void clearAll(Long userId) {
        notificationRepository.deleteByUserId(userId);
    }

    private List<Long> getAllEventUsers(Long eventId) {
        return registrationServiceClient.getAllEventUsers(eventId);
    }

    private void createNotifications(Long eventId, String message, NotificationType type) {
        List<Notification> notifications = getAllEventUsers(eventId).stream()
                .map(userId -> Notification.builder()
                        .userId(userId)
                        .type(type)
                        .message(message)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }

    private Notification findAndAssertOwner(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + id));

        if (!notification.getUserId().equals(userId)) {
            throw new SecurityException("Access Denied");
        }

        return notification;
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
