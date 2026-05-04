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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            HttpServletRequest httpRequest
    ) {
        Long organizerId = (Long) httpRequest.getAttribute("userId");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(request, organizerId));
    }

    @GetMapping
    public ResponseEntity<Page<EventResponse>> getPublishedEvents(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(eventService.getPublishedEvents(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @RequestBody UpdateEventRequest request,
            HttpServletRequest httpRequest
    ) {
        Long requesterId = (Long) httpRequest.getAttribute("userId");

        return ResponseEntity.ok(eventService.updateEvent(id, request, requesterId));
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
}
