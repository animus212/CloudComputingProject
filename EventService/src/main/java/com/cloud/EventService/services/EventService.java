package com.cloud.EventService.services;

import com.cloud.EventService.configs.RabbitMQConfig;
import com.cloud.EventService.dtos.requests.CreateEventRequest;
import com.cloud.EventService.dtos.requests.UpdateEventRequest;
import com.cloud.EventService.dtos.responses.EventResponse;
import com.cloud.EventService.dtos.responses.EventSummaryDto;
import com.cloud.EventService.entities.Event;
import com.cloud.EventService.entities.EventStatus;
import com.cloud.EventService.events.EventCreatedEvent;
import com.cloud.EventService.exceptions.EventCapacityException;
import com.cloud.EventService.exceptions.EventNotFoundException;
import com.cloud.EventService.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventRepository eventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public EventResponse createEvent(CreateEventRequest request, Long organizerId) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .capacity(request.getCapacity())
                .organizerId(organizerId)
                .eventType(request.getEventType())
                .status(EventStatus.DRAFT)
                .build();

        event = eventRepository.save(event);

        log.info("Event created: id={}, title={}", event.getId(), event.getTitle());

        publishEventCreatedEvent(event);

        return mapToResponse(event);
    }

    public Page<EventResponse> getPublishedEvents(Pageable pageable) {
        return eventRepository.findByStatus(EventStatus.PUBLISHED, pageable)
                .map(this::mapToResponse);
    }

    public EventResponse getEventById(Long id) {
        return mapToResponse(findEventOrThrow(id));
    }

    @Transactional
    public EventResponse updateEvent(Long id, UpdateEventRequest request, Long requesterId) {
        Event event = findEventOrThrow(id);

        if (!event.getOrganizerId().equals(requesterId)) {
            throw new SecurityException("Only the organizer can update this event");
        }

        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getStartDate() != null) event.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) event.setEndDate(request.getEndDate());
        if (request.getCapacity() != null) event.setCapacity(request.getCapacity());
        if (request.getEventType() != null) event.setEventType(request.getEventType());
        if (request.getStatus() != null) event.setStatus(request.getStatus());

        return mapToResponse(eventRepository.save(event));
    }

    @Transactional
    public EventSummaryDto reserveSpot(Long eventId) {
        Event event = findEventOrThrow(eventId);

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new EventCapacityException("Event is not open for registration");
        }

        int updated = eventRepository.incrementRegisteredCount(eventId);

        if (updated == 0) {
            throw new EventCapacityException("No available spots for event: " + eventId);
        }

        return EventSummaryDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .available(true)
                .availableSpots(event.getAvailableSpots() - 1)
                .build();
    }

    @Transactional
    public void releaseSpot(Long eventId) {
        findEventOrThrow(eventId);

        eventRepository.decrementRegisteredCount(eventId);

        log.debug("Released spot for event: {}", eventId);
    }

    private Event findEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + id));
    }

    private void publishEventCreatedEvent(Event event) {
        try {
            EventCreatedEvent message = EventCreatedEvent.builder()
                    .eventId(event.getId())
                    .title(event.getTitle())
                    .location(event.getLocation())
                    .startDate(event.getStartDate())
                    .organizerId(event.getOrganizerId())
                    .createdAt(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.EVENT_CREATED_ROUTING_KEY,
                    message
            );
        } catch (Exception e) {
            log.error("Failed to publish EventCreatedEvent: {}", e.getMessage());
        }
    }

    private EventResponse mapToResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .capacity(event.getCapacity())
                .registeredCount(event.getRegisteredCount())
                .availableSpots(event.getAvailableSpots())
                .organizerId(event.getOrganizerId())
                .status(event.getStatus())
                .eventType(event.getEventType())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
