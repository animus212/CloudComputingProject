package com.cloud.RegistrationService.events;

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
public class RegistrationCreatedEvent implements Serializable {
    private Long registrationId;
    private Long userId;
    private String userEmail;
    private Long eventId;
    private String eventTitle;
    private LocalDateTime registeredAt;
}
