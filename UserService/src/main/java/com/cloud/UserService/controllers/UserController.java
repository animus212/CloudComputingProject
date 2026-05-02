package com.cloud.UserService.controllers;

import com.cloud.UserService.dtos.responses.UserResponse;
import com.cloud.UserService.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserByUsername(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #userDetails.username == @userService.getUserById(#id).username")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
