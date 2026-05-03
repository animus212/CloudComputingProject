package com.cloud.NotificationService.events;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RegistrationCreatedEvent {
    private Long registrationId;
    private Long userId;
    private String userEmail;
    private Long eventId;
    private String eventTitle;
    private LocalDateTime registeredAt;
}
