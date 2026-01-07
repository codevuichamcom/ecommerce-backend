package com.ecommerce.notification.application.dto;

import java.util.Map;

/**
 * Command to send a notification.
 */
public record SendNotificationCommand(
        String recipientId,
        String recipientEmail,
        String notificationType,
        String subject,
        String message,
        Map<String, String> metadata) {
}
