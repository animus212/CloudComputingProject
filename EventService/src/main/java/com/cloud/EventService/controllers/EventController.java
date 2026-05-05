package com.cloud.EventService.controllers;

import com.cloud.EventService.dtos.requests.CreateEventRequest;
import com.cloud.EventService.dtos.requests.UpdateEventRequest;
import com.cloud.EventService.dtos.responses.EventResponse;
import com.cloud.EventService.dtos.responses.EventSummaryDto;
import com.cloud.EventService.services.EventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            HttpServletRequest httpRequest
    ) {
        Long organizerId = getUserId(httpRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(request, organizerId));
    }

    @GetMapping
    public ResponseEntity<Page<EventResponse>> getPublishedEvents(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(eventService.getPublishedEvents(pageable));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<EventResponse>> getMyEvents(HttpServletRequest httpRequest) {
        Long organizerId = getUserId(httpRequest);

        return ResponseEntity.ok(eventService.getOrganizerEvents(organizerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @RequestBody UpdateEventRequest request,
            HttpServletRequest httpRequest
    ) {
        Long requesterId = getUserId(httpRequest);

        return ResponseEntity.ok(eventService.updateEvent(id, request, requesterId));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> publishEvent(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long requesterId = getUserId(httpRequest);

        return ResponseEntity.ok(eventService.publishEvent(id, requesterId));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> cancelEvent(
            @PathVariable Long id,
            HttpServletRequest httpRequest,
            Authentication auth
    ) {
        Long requesterId = getUserId(httpRequest);

        return ResponseEntity.ok(eventService.cancelEvent(id, requesterId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long requesterId = getUserId(httpRequest);

        eventService.deleteEvent(id, requesterId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<EventSummaryDto> reserveSpot(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.reserveSpot(id));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<Void> releaseSpot(@PathVariable Long id) {
        eventService.releaseSpot(id);

        return ResponseEntity.noContent().build();
    }

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }
}
