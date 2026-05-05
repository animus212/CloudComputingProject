package com.cloud.RegistrationService.controllers;

import com.cloud.RegistrationService.dtos.requests.CreateRegistrationRequest;
import com.cloud.RegistrationService.dtos.response.RegistrationResponse;
import com.cloud.RegistrationService.services.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody CreateRegistrationRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserId(httpRequest);
        String userEmail = (String) httpRequest.getAttribute("userEmail");
        String token = extractToken(httpRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registrationService.createRegistration(request, userId, userEmail, token));
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
            @PathVariable Long eventId,
            HttpServletRequest httpRequest
    ) {
        Long requesterId = getUserId(httpRequest);

        return ResponseEntity.ok(registrationService.getEventRegistrations(eventId, requesterId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RegistrationResponse> cancelRegistration(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserId(httpRequest);
        String token = extractToken(httpRequest);

        return ResponseEntity.ok(registrationService.cancelRegistration(id, userId, token));
    }

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }
}
