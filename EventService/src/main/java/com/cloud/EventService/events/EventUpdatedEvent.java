package com.cloud.EventService.events;

import com.cloud.EventService.entities.EventStatus;
import com.cloud.EventService.entities.EventType;
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
public class EventUpdatedEvent implements Serializable {
    private Long eventId;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long organizerId;
    private EventType eventType;
    private EventStatus eventStatus;
    private LocalDateTime updatedAt;
}
