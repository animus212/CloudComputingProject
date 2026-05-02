package com.cloud.UserService.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent implements Serializable {
    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private LocalDateTime registeredAt;
}
