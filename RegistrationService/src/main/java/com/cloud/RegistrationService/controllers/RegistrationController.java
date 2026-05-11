package com.cloud.RegistrationService.controllers;

import com.cloud.RegistrationService.dtos.response.RegistrationResponse;
import com.cloud.RegistrationService.services.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;

    @PostMapping("/event/{eventId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RegistrationResponse> register(
            @PathVariable Long eventId,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserId(httpRequest);
        String token = httpRequest.getHeader("Authorization");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registrationService.createRegistration(eventId, userId,token));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<RegistrationResponse>> getMyRegistrations(
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserId(httpRequest);

        return ResponseEntity.ok(registrationService.getUserRegistrations(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistrationResponse> getRegistration(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserId(httpRequest);

        return ResponseEntity.ok(registrationService.getRegistration(id, userId));
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<RegistrationResponse>> getEventRegistrations(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(registrationService.getEventRegistrations(eventId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RegistrationResponse> cancelRegistration(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserId(httpRequest);
        String token = httpRequest.getHeader("Authorization");
        return ResponseEntity.ok(registrationService.cancelRegistration(id, userId,token));
    }

    @GetMapping("/event/internal/{eventId}")
    public ResponseEntity<List<Long>> getAllEventUsers(
            @PathVariable Long eventId
    ) {
        return  ResponseEntity.ok(registrationService.getAllEventUsers(eventId));
    }


    @GetMapping("/event/internal/{eventId}/all")
    public ResponseEntity<List<Long>> getAllEventUsersIncludingCancelled(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(
                registrationService.getAllEventUsersIncludingCancelled(eventId));
    }

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }
}
