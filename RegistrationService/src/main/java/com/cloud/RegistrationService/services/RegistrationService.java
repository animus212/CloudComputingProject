package com.cloud.RegistrationService.services;

import com.cloud.RegistrationService.clients.EventServiceClient;
import com.cloud.RegistrationService.dtos.response.EventSummaryDto;
import com.cloud.RegistrationService.dtos.response.RegistrationResponse;
import com.cloud.RegistrationService.entities.Payment;
import com.cloud.RegistrationService.entities.PaymentStatus;
import com.cloud.RegistrationService.entities.Registration;
import com.cloud.RegistrationService.entities.RegistrationStatus;
import com.cloud.RegistrationService.exceptions.DuplicateRegistrationException;
import com.cloud.RegistrationService.exceptions.RegistrationNotFoundException;
import com.cloud.RegistrationService.repositories.PaymentRepository;
import com.cloud.RegistrationService.repositories.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final EventServiceClient eventServiceClient;
    private final PaymentRepository paymentRepository;

    @Transactional
    public RegistrationResponse createRegistration(
            Long eventId,
            Long userId,
            String token
    ) {
        boolean alreadyRegistered = registrationRepository.existsByUserIdAndEventIdAndStatusNot(
                userId, eventId, RegistrationStatus.CANCELLED
        );

        if (alreadyRegistered) {
            throw new DuplicateRegistrationException(
                    "User " + userId + " is already registered for event " + eventId
            );
        }

        EventSummaryDto eventSummary = eventServiceClient.reserveSpot(eventId,token);

        Registration registration = Registration.builder()
                .userId(userId)
                .eventId(eventId)
                .eventTitle(eventSummary.getTitle())
                .status(RegistrationStatus.CONFIRMED)
                .build();

        registration = registrationRepository.save(registration);


        Payment payment = null;
        if (eventSummary.getPrice() != null && eventSummary.getPrice() > 0) {
            payment = paymentRepository.save(Payment.builder()
                    .registrationId(eventSummary.getId())
                    .userId(userId)
                    .eventId(eventId)
                    .amount(eventSummary.getPrice())
                    .status(PaymentStatus.COMPLETED)
                    .build());
            log.info("Payment processed: ${} for registration={}", eventSummary.getPrice(), registration.getId());
        }
        log.info("Registration created: id={}, user={}, event={}", registration.getId(), userId, eventId);

        return mapToResponse(registration, paymentRepository.findByRegistrationId(registration.getId()).orElse(null));
    }

    public List<RegistrationResponse> getUserRegistrations(Long userId) {
        return registrationRepository.findByUserId(userId).stream()
                .map(reg -> mapToResponse(reg, paymentRepository.findByRegistrationId(reg.getId()).orElse(null)))
                .toList();
    }

    public RegistrationResponse getRegistration(Long id, Long userId) {
        Registration reg = findRegistrationOrThrow(id);
        if (!reg.getUserId().equals(userId)) throw new SecurityException("Access denied");
        return mapToResponse(reg, paymentRepository.findByRegistrationId(id).orElse(null));
    }

    public List<RegistrationResponse> getEventRegistrations(Long eventId) {
        return registrationRepository.findByEventId(eventId).stream()
                .map(reg -> mapToResponse(reg, paymentRepository.findByRegistrationId(reg.getId()).orElse(null)))
                .toList();
    }

    public List<Long> getAllEventUsers(Long eventId) {
        return registrationRepository.findUserIdsByEventIdAndStatusNot(eventId, RegistrationStatus.CANCELLED);
    }

    @Transactional
    public RegistrationResponse cancelRegistration(Long registrationId, Long userId, String token) {
        Registration registration = findRegistrationOrThrow(registrationId);

        if (!registration.getUserId().equals(userId)) {
            throw new SecurityException("You can only cancel your own registrations");
        }

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new IllegalStateException("Registration is already cancelled");
        }

        registration.setStatus(RegistrationStatus.CANCELLED);
        registrationRepository.save(registration);

        Payment payment = paymentRepository.findByRegistrationId(registrationId).orElse(null);
        if (payment != null) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            log.info("Payment refunded: ${} for registration={}", payment.getAmount(), registrationId);
        }

        eventServiceClient.releaseSpot(registration.getEventId(), token);

        return mapToResponse(registration, payment);
    }

    private Registration findRegistrationOrThrow(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new RegistrationNotFoundException("Registration not found: " + id));
    }

    private RegistrationResponse mapToResponse(Registration reg, Payment payment) {
        return RegistrationResponse.builder()
                .id(reg.getId())
                .userId(reg.getUserId())
                .eventId(reg.getEventId())
                .eventTitle(reg.getEventTitle())
                .status(reg.getStatus())
                .registeredAt(reg.getRegisteredAt())
                .amountPaid(payment != null ? payment.getAmount() : 0.0)
                .paymentStatus(payment != null ? payment.getStatus() : null)
                .build();
    }
}
