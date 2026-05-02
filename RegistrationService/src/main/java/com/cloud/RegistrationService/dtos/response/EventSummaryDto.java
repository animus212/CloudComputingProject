package com.cloud.RegistrationService.dtos.response;

import lombok.Data;

@Data
public class EventSummaryDto {
    private Long id;
    private String title;
    private boolean available;
    private int availableSpots;
}
