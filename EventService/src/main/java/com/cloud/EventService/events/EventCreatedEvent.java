package com.cloud.EventService.events;

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
public class EventCreatedEvent implements Serializable {
    private Long eventId;
    private String title;
    private String location;
    private LocalDateTime startDate;
    private Long organizerId;
    private LocalDateTime createdAt;
}
