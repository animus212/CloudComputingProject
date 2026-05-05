package com.cloud.NotificationService.dtos.responses;

import com.cloud.NotificationService.entities.NotificationStatus;
import com.cloud.NotificationService.entities.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private Long userId;
    private NotificationType type;
    private String subject;
    private String message;
    private NotificationStatus status;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
