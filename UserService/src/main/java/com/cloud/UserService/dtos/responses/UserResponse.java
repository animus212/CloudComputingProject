package com.cloud.UserService.dtos.responses;

import com.cloud.UserService.entities.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private LocalDateTime createdAt;
}
