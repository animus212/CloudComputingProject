package com.cloud.RegistrationService.repositories;

import com.cloud.RegistrationService.entities.Registration;
import com.cloud.RegistrationService.entities.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByUserId(Long userId);
    List<Registration> findByEventId(Long eventId);
    @Query("SELECT r.userId FROM Registration r WHERE r.eventId = :eventId AND r.status <> :status")
    List<Long> findUserIdsByEventIdAndStatusNot(
            @Param("eventId") Long eventId,
            @Param("status") RegistrationStatus status
    );
    boolean existsByUserIdAndEventIdAndStatusNot(Long userId, Long eventId, RegistrationStatus status);
}
