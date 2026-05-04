package com.cloud.NotificationService.repositories;

import com.cloud.NotificationService.entities.Notification;
import com.cloud.NotificationService.entities.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findByStatus(NotificationStatus status);
}
