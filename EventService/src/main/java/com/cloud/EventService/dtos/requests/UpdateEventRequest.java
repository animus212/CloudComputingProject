package com.cloud.EventService.dtos.requests;

import com.cloud.EventService.entities.EventStatus;
import com.cloud.EventService.entities.EventType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateEventRequest {
    private String title;
    private String description;
    private String location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer capacity;
    private EventType eventType;
    private EventStatus status;
}
