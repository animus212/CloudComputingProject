package com.cloud.RegistrationService.repositories;

import com.cloud.RegistrationService.entities.Registration;
import com.cloud.RegistrationService.entities.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByUserId(Long userId);
    List<Registration> findByEventId(Long eventId);
    Optional<Registration> findByUserIdAndEventId(Long userId, Long eventId);
    boolean existsByUserIdAndEventIdAndStatusNot(Long userId, Long eventId, RegistrationStatus status);
}
