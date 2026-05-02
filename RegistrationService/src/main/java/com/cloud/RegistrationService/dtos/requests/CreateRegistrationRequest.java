package com.cloud.RegistrationService.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRegistrationRequest {
    @NotNull(message = "Event ID is required")
    private Long eventId;
}
