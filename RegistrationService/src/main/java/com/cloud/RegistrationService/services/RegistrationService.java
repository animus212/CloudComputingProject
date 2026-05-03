package com.cloud.RegistrationService.services;

import com.cloud.RegistrationService.clients.EventServiceClient;
import com.cloud.RegistrationService.configs.RabbitMQConfig;
import com.cloud.RegistrationService.dtos.requests.CreateRegistrationRequest;
import com.cloud.RegistrationService.dtos.response.EventSummaryDto;
import com.cloud.RegistrationService.dtos.response.RegistrationResponse;
import com.cloud.RegistrationService.entities.Registration;
import com.cloud.RegistrationService.entities.RegistrationStatus;
import com.cloud.RegistrationService.events.RegistrationCancelledEvent;
import com.cloud.RegistrationService.events.RegistrationCreatedEvent;
import com.cloud.RegistrationService.exceptions.DuplicateRegistrationException;
import com.cloud.RegistrationService.exceptions.RegistrationNotFoundException;
import com.cloud.RegistrationService.repositories.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final EventServiceClient eventServiceClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public RegistrationResponse createRegistration(CreateRegistrationRequest request, Long userId, String userEmail) {
        Long eventId = request.getEventId();

        boolean alreadyRegistered = registrationRepository.existsByUserIdAndEventIdAndStatusNot(
                userId, eventId, RegistrationStatus.CANCELLED
        );

        if (alreadyRegistered) {
            throw new DuplicateRegistrationException(
                    "User " + userId + " is already registered for event " + eventId
            );
        }

        EventSummaryDto eventSummary = eventServiceClient.reserveSpot(eventId);

        Registration registration = Registration.builder()
                .userId(userId)
                .userEmail(userEmail)
                .eventId(eventId)
                .eventTitle(eventSummary.getTitle())
                .status(RegistrationStatus.CONFIRMED)
                .build();

        registration = registrationRepository.save(registration);

        log.info("Registration created: id={}, user={}, event={}", registration.getId(), userId, eventId);

        publishRegistrationCreatedEvent(registration);

        return mapToResponse(registration);
    }

    @Transactional
    public RegistrationResponse cancelRegistration(Long registrationId, Long userId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException("Registration not found: " + registrationId));

        if (!registration.getUserId().equals(userId)) {
            throw new SecurityException("You can only cancel your own registrations");
        }

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new IllegalStateException("Registration is already cancelled");
        }

        registration.setStatus(RegistrationStatus.CANCELLED);
        registrationRepository.save(registration);

        // Release the spot back in Event Service
        eventServiceClient.releaseSpot(registration.getEventId());

        publishRegistrationCancelledEvent(registration);

        return mapToResponse(registration);
    }

    public List<RegistrationResponse> getUserRegistrations(Long userId) {
        return registrationRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RegistrationResponse getRegistration(Long id, Long userId) {
        Registration reg = registrationRepository.findById(id)
                .orElseThrow(() -> new RegistrationNotFoundException("Registration not found: " + id));
        if (!reg.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }
        return mapToResponse(reg);
    }

    private void publishRegistrationCreatedEvent(Registration reg) {
        try {
            RegistrationCreatedEvent event = RegistrationCreatedEvent.builder()
                    .registrationId(reg.getId())
                    .userId(reg.getUserId())
                    .userEmail(reg.getUserEmail())
                    .eventId(reg.getEventId())
                    .eventTitle(reg.getEventTitle())
                    .registeredAt(reg.getRegisteredAt())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.REGISTRATION_CREATED_ROUTING_KEY,
                    event
            );
        } catch (Exception e) {
            log.error("Failed to publish RegistrationCreatedEvent: {}", e.getMessage());
        }
    }

    private void publishRegistrationCancelledEvent(Registration reg) {
        try {
            RegistrationCancelledEvent event = RegistrationCancelledEvent.builder()
                    .registrationId(reg.getId())
                    .userId(reg.getUserId())
                    .userEmail(reg.getUserEmail())
                    .eventId(reg.getEventId())
                    .eventTitle(reg.getEventTitle())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.REGISTRATION_CANCELLED_ROUTING_KEY,
                    event
            );
        } catch (Exception e) {
            log.error("Failed to publish RegistrationCancelledEvent: {}", e.getMessage());
        }
    }

    private RegistrationResponse mapToResponse(Registration reg) {
        return RegistrationResponse.builder()
                .id(reg.getId())
                .userId(reg.getUserId())
                .eventId(reg.getEventId())
                .eventTitle(reg.getEventTitle())
                .status(reg.getStatus())
                .registeredAt(reg.getRegisteredAt())
                .build();
    }
}
