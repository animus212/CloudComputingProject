package com.cloud.NotificationService.events;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserRegisteredEvent {
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private LocalDateTime registeredAt;
}
