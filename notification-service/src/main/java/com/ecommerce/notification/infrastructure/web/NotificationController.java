package com.ecommerce.notification.infrastructure.web;

import com.ecommerce.notification.application.dto.NotificationResponse;
import com.ecommerce.notification.application.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for notification operations.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Get notification by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable String id) {
        NotificationResponse notification = notificationService.getNotification(id);
        return ResponseEntity.ok(notification);
    }

    /**
     * Get notifications for a recipient.
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotificationsForRecipient(
            @RequestParam String recipientId) {
        List<NotificationResponse> notifications = notificationService.getNotificationsForRecipient(recipientId);
        return ResponseEntity.ok(notifications);
    }
}
