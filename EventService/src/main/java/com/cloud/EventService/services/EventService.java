package com.cloud.EventService.services;

import com.cloud.EventService.configs.RabbitMQConfig;
import com.cloud.EventService.dtos.requests.CreateEventRequest;
import com.cloud.EventService.dtos.requests.UpdateEventRequest;
import com.cloud.EventService.dtos.responses.EventResponse;
import com.cloud.EventService.dtos.responses.EventSummaryDto;
import com.cloud.EventService.entities.Event;
import com.cloud.EventService.entities.EventStatus;
import com.cloud.EventService.events.EventUpdatedEvent;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventRepository eventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public EventResponse createEvent(CreateEventRequest request, Long organizerId) {
        if (request.getEndDate().isBefore(request.getStartDate()))
            throw new IllegalArgumentException("End date must be after start date");

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .capacity(request.getCapacity())
                .organizerId(organizerId)
                .eventType(request.getEventType())
                .price(request.getPrice() != null ? request.getPrice() : 0.0)
                .status(EventStatus.DRAFT)
                .build();

        event = eventRepository.save(event);
        log.info("Event created: id={}, title={}", event.getId(), event.getTitle());
        return mapToResponse(event);
    }

    public Page<EventResponse> getPublishedEvents(Pageable pageable) {
        return eventRepository.findByStatus(EventStatus.PUBLISHED, pageable).map(this::mapToResponse);
    }

    public List<EventResponse> getOrganizerEvents(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId).stream().map(this::mapToResponse).toList();
    }

    public EventResponse getEventById(Long id) {
        return mapToResponse(findEventOrThrow(id));
    }

    @Transactional
    public EventResponse updateEvent(Long id, UpdateEventRequest request, Long requesterId) {
        Event event = findEventOrThrow(id);
        assertOrganizer(event, requesterId);

        if (request.getEndDate().isBefore(request.getStartDate()))
            throw new IllegalArgumentException("End date must be after start date");
        if (request.getCapacity() < event.getRegisteredCount())
            throw new IllegalArgumentException("Capacity must not be lower than the number of registered users");

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setCapacity(request.getCapacity());
        event.setEventType(request.getEventType());
        event.setPrice(request.getPrice() != null ? request.getPrice() : 0.0);

        event = eventRepository.save(event);
        publishEventUpdatedEvent(event);
        return mapToResponse(event);
    }

    @Transactional
    public EventResponse publishEvent(Long id, Long requesterId) {
        Event event = findEventOrThrow(id);
        assertOrganizer(event, requesterId);
        if (event.getStatus() != EventStatus.DRAFT)
            throw new IllegalStateException("Only DRAFT events can be published");
        event.setStatus(EventStatus.PUBLISHED);
        log.info("Event published: id={}", id);
        return mapToResponse(eventRepository.save(event));
    }

    @Transactional
    public EventResponse cancelEvent(Long id, Long requesterId) {
        Event event = findEventOrThrow(id);
        assertOrganizer(event, requesterId);
        if (event.getStatus() == EventStatus.CANCELLED)
            throw new IllegalStateException("Event is already cancelled");
        if (event.getStatus() == EventStatus.COMPLETED)
            throw new IllegalStateException("Cannot cancel a completed event");
        event.setStatus(EventStatus.CANCELLED);
        event = eventRepository.save(event);
        log.info("Event cancelled: id={}", id);
        publishEventUpdatedEvent(event);
        return mapToResponse(event);
    }

    @Transactional
    public EventResponse completeEvent(Long id, Long requesterId) {
        Event event = findEventOrThrow(id);
        assertOrganizer(event, requesterId);
        if (event.getStatus() != EventStatus.PUBLISHED)
            throw new IllegalStateException("Only PUBLISHED events can be completed");
        if (event.getStartDate().isAfter(LocalDateTime.now()))
            throw new IllegalStateException("Can not complete an event before the start date");
        event.setStatus(EventStatus.COMPLETED);
        event = eventRepository.save(event);
        log.info("Event completed: id={}", id);
        publishEventUpdatedEvent(event);
        return mapToResponse(event);
    }

    @Transactional
    public void deleteEvent(Long id, Long requesterId) {
        Event event = findEventOrThrow(id);
        assertOrganizer(event, requesterId);
        if (event.getStatus() == EventStatus.PUBLISHED && event.getRegisteredCount() > 0)
            throw new IllegalStateException("Cannot delete a published event with active registrations. Cancel it first.");
        eventRepository.delete(event);
        log.info("Event deleted: id={}, by={}", id, requesterId);
    }

    @Transactional
    public EventSummaryDto reserveSpot(Long eventId) {
        Event event = findEventOrThrow(eventId);
        if (event.getStatus() != EventStatus.PUBLISHED)
            throw new EventCapacityException("Event is not open for registration");

        int updated = eventRepository.incrementRegisteredCount(eventId);
        if (updated == 0)
            throw new EventCapacityException("No available spots for event: " + eventId);

        Event refreshed = findEventOrThrow(eventId);
        return EventSummaryDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .available(true)
                .availableSpots(refreshed.getAvailableSpots())
                .price(event.getPrice())
                .build();
    }

    @Transactional
    public void releaseSpot(Long eventId) {
        findEventOrThrow(eventId);
        int updated = eventRepository.decrementRegisteredCount(eventId);
        if (updated == 0)
            throw new EventCapacityException("No spots to release for event: " + eventId);
        log.debug("Released spot for event: {}", eventId);
    }

    private Event findEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + id));
    }

    private void assertOrganizer(Event event, Long requesterId) {
        if (!event.getOrganizerId().equals(requesterId))
            throw new SecurityException("Only the organizer can perform this action");
    }

    private void publishEventUpdatedEvent(Event event) {
        try {
            EventUpdatedEvent msg = EventUpdatedEvent.builder()
                    .eventId(event.getId())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .location(event.getLocation())
                    .startDate(event.getStartDate())
                    .endDate(event.getEndDate())
                    .organizerId(event.getOrganizerId())
                    .eventStatus(event.getStatus())
                    .eventType(event.getEventType())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.EVENT_UPDATED_ROUTING_KEY, msg);
        } catch (Exception e) {
            log.error("Failed to publish EventUpdatedEvent: {}", e.getMessage());
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
                .price(event.getPrice())
                .createdAt(event.getCreatedAt())
                .build();
    }
}