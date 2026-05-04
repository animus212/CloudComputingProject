package com.cloud.EventService.dtos.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventSummaryDto {
    private Long id;
    private String title;
    private boolean available;
    private int availableSpots;
}
