package com.ecommerce.notification.application.dto;

import com.ecommerce.notification.domain.model.Notification;

import java.time.Instant;

/**
 * Notification response DTO.
 */
public record NotificationResponse(
        String id,
        String recipientId,
        String recipientEmail,
        String type,
        String channel,
        String subject,
        String message,
        String status,
        Instant createdAt,
        Instant sentAt) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId().value(),
                notification.getRecipientId(),
                notification.getRecipientEmail(),
                notification.getType().name(),
                notification.getChannel().name(),
                notification.getSubject(),
                notification.getMessage(),
                notification.getStatus().name(),
                notification.getCreatedAt(),
                notification.getSentAt());
    }
}
