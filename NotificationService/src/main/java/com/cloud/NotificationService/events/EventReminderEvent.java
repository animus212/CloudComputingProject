package com.cloud.NotificationService.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventReminderEvent implements Serializable {
    private Long eventId;
    private String title;
    private LocalDateTime startDate;
}
