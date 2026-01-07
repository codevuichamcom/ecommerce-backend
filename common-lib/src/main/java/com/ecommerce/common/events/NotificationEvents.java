package com.ecommerce.common.events;

import com.ecommerce.common.domain.DomainEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Notification-related domain events.
 * These events trigger notifications to be sent to users.
 */
public sealed interface NotificationEvents extends DomainEvent {

    /**
     * Recipient of the notification.
     */
    String recipientId();

    // ==================== Send Email Notification ====================

    /**
     * Request to send an email notification.
     */
    record SendEmailNotification(
            String eventId,
            Instant occurredAt,
            String recipientId,
            String recipientEmail,
            String templateName,
            String subject,
            Map<String, String> templateVariables) implements NotificationEvents {

        public static SendEmailNotification create(
                String recipientId,
                String recipientEmail,
                String templateName,
                String subject,
                Map<String, String> templateVariables) {
            return new SendEmailNotification(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    recipientId,
                    recipientEmail,
                    templateName,
                    subject,
                    templateVariables);
        }
    }

    // ==================== Order Notification ====================

    /**
     * Generic order-related notification.
     */
    record OrderNotification(
            String eventId,
            Instant occurredAt,
            String recipientId,
            String orderId,
            NotificationType type,
            String message) implements NotificationEvents {

        public enum NotificationType {
            ORDER_CREATED,
            ORDER_CONFIRMED,
            ORDER_SHIPPED,
            ORDER_DELIVERED,
            ORDER_CANCELLED,
            PAYMENT_RECEIVED,
            PAYMENT_FAILED,
            REFUND_PROCESSED
        }

        public static OrderNotification create(
                String recipientId,
                String orderId,
                NotificationType type,
                String message) {
            return new OrderNotification(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    recipientId,
                    orderId,
                    type,
                    message);
        }
    }
}
