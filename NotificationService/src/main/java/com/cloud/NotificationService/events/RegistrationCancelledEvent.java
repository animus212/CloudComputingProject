package com.cloud.NotificationService.events;

import lombok.Data;

@Data
public class RegistrationCancelledEvent {
    private Long registrationId;
    private Long userId;
    private String userEmail;
    private Long eventId;
    private String eventTitle;
}
