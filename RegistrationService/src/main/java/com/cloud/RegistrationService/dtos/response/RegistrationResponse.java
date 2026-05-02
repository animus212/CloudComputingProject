package com.cloud.RegistrationService.dtos.response;

import com.cloud.RegistrationService.entities.RegistrationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RegistrationResponse {
    private Long id;
    private Long userId;
    private Long eventId;
    private String eventTitle;
    private RegistrationStatus status;
    private LocalDateTime registeredAt;
}
