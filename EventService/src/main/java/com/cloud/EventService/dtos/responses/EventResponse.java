package com.cloud.EventService.dtos.responses;

import com.cloud.EventService.entities.EventStatus;
import com.cloud.EventService.entities.EventType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer capacity;
    private Integer registeredCount;
    private Integer availableSpots;
    private Long organizerId;
    private EventStatus status;
    private EventType eventType;
    private LocalDateTime createdAt;
}
