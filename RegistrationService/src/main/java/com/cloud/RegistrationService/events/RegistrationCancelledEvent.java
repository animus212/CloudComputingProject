package com.cloud.RegistrationService.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationCancelledEvent implements Serializable {
    private Long registrationId;
    private Long userId;
    private String userEmail;
    private Long eventId;
    private String eventTitle;
}
