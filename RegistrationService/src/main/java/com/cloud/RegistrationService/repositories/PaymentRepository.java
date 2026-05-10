package com.cloud.RegistrationService.repositories;

import com.cloud.RegistrationService.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRegistrationId(Long registrationId);
}