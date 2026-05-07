package com.cloud.NotificationService.controllers;

import com.cloud.NotificationService.dtos.responses.NotificationResponse;
import com.cloud.NotificationService.services.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");

        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");

        notificationService.markAsRead(userId, id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");

        notificationService.markAllAsRead(userId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");

        notificationService.deleteNotification(userId, id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearAll(HttpServletRequest req) {
        notificationService.clearAll((Long) req.getAttribute("userId"));

        return ResponseEntity.noContent().build();
    }
}
