package com.cloud.NotificationService.services;

import com.cloud.NotificationService.entities.Notification;
import com.cloud.NotificationService.entities.NotificationStatus;
import com.cloud.NotificationService.entities.NotificationType;
import com.cloud.NotificationService.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    public void sendWelcomeNotification(Long userId, String email, String firstName) {
        String subject = "Welcome to Event Management System!";
        String message = String.format(
                "Hi %s,\n\nWelcome! Your account has been created successfully.\n\nEnjoy exploring events!",
                firstName != null ? firstName : "there"
        );

        createAndSend(userId, email, NotificationType.WELCOME, subject, message);
    }

    public void sendRegistrationConfirmation(Long userId, String email, String eventTitle, LocalDateTime registeredAt) {
        String subject = "Registration Confirmed: " + eventTitle;
        String message = String.format(
                "Your registration for \"%s\" has been confirmed!\n\nRegistered at: %s",
                eventTitle, registeredAt
        );

        createAndSend(userId, email, NotificationType.REGISTRATION_CONFIRMATION, subject, message);
    }

    public void sendCancellationNotification(Long userId, String email, String eventTitle) {
        String subject = "Registration Cancelled: " + eventTitle;
        String message = String.format(
                "Your registration for \"%s\" has been cancelled.", eventTitle
        );

        createAndSend(userId, email, NotificationType.REGISTRATION_CANCELLATION, subject, message);
    }

    private void createAndSend(Long userId, String email, NotificationType type, String subject, String message) {
        Notification notification = Notification.builder()
                .userId(userId)
                .recipientEmail(email)
                .type(type)
                .subject(subject)
                .message(message)
                .status(NotificationStatus.PENDING)
                .build();

        notification = notificationRepository.save(notification);

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(email);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            mailSender.send(mailMessage);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());

            log.info("Notification sent: type={}, to={}", type, email);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());

            log.error("Failed to send notification: type={}, to={}, error={}", type, email, e.getMessage());
        }

        notificationRepository.save(notification);
    }
}
