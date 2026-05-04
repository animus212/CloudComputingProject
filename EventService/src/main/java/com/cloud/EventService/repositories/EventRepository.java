package com.cloud.EventService.repositories;

import com.cloud.EventService.entities.Event;
import com.cloud.EventService.entities.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    List<Event> findByOrganizerId(Long organizerId);

    @Modifying
    @Query("UPDATE Event e SET e.registeredCount = e.registeredCount + 1 WHERE e.id = :id AND e.registeredCount < e.capacity")
    int incrementRegisteredCount(Long id);

    @Modifying
    @Query("UPDATE Event e SET e.registeredCount = e.registeredCount - 1 WHERE e.id = :id AND e.registeredCount > 0")
    int decrementRegisteredCount(Long id);
}
