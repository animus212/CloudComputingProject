package com.cloud.NotificationService.controllers;

import com.cloud.NotificationService.dtos.responses.NotificationResponse;
import com.cloud.NotificationService.entities.Notification;
import com.cloud.NotificationService.repositories.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository notificationRepository;

    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");

        return ResponseEntity.ok(notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse).toList());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        Notification notification = findAndAssertOwner(id, userId);

        notification.setRead(true);

        return ResponseEntity.ok(mapToResponse(notificationRepository.save(notification)));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(userId);

        unread.forEach(n -> n.setRead(true));

        notificationRepository.saveAll(unread);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");

        notificationRepository.delete(findAndAssertOwner(id, userId));

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearAll(HttpServletRequest req) {
        notificationRepository.deleteByUserId((Long) req.getAttribute("userId"));

        return ResponseEntity.noContent().build();
    }

    private Notification findAndAssertOwner(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + id));

        if (!notification.getUserId().equals(userId)) {
            throw new SecurityException("Access Denied");
        }

        return notification;
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .build();
    }
}
