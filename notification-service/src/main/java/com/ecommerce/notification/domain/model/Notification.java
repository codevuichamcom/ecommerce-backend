package com.ecommerce.notification.domain.model;

import com.github.f4b6a3.ulid.UlidCreator;

import java.time.Instant;
import java.util.Map;

/**
 * Notification entity.
 * Represents a notification to be sent to a user.
 */
public class Notification {

    private final NotificationId id;
    private final String recipientId;
    private final String recipientEmail;
    private final NotificationType type;
    private final NotificationChannel channel;
    private final String subject;
    private final String message;
    private final Map<String, String> metadata;
    private NotificationStatus status;
    private String messageId;
    private String failureReason;
    private Instant createdAt;
    private Instant sentAt;

    private Notification(
            NotificationId id,
            String recipientId,
            String recipientEmail,
            NotificationType type,
            NotificationChannel channel,
            String subject,
            String message,
            Map<String, String> metadata,
            NotificationStatus status,
            Instant createdAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.recipientEmail = recipientEmail;
        this.type = type;
        this.channel = channel;
        this.subject = subject;
        this.message = message;
        this.metadata = metadata;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Create a new email notification.
     */
    public static Notification createEmail(
            String recipientId,
            String recipientEmail,
            NotificationType type,
            String subject,
            String message,
            Map<String, String> metadata) {
        return new Notification(
                new NotificationId(UlidCreator.getUlid().toString()),
                recipientId,
                recipientEmail,
                type,
                NotificationChannel.EMAIL,
                subject != null ? subject : type.getTitle(),
                message != null ? message : type.getDefaultMessage(),
                metadata != null ? metadata : Map.of(),
                new NotificationStatus.Pending(),
                Instant.now());
    }

    /**
     * Reconstitute from persistence.
     */
    public static Notification reconstitute(
            NotificationId id,
            String recipientId,
            String recipientEmail,
            NotificationType type,
            NotificationChannel channel,
            String subject,
            String message,
            Map<String, String> metadata,
            NotificationStatus status,
            String messageId,
            String failureReason,
            Instant createdAt,
            Instant sentAt) {
        Notification notification = new Notification(
                id, recipientId, recipientEmail, type, channel,
                subject, message, metadata, status, createdAt);
        notification.messageId = messageId;
        notification.failureReason = failureReason;
        notification.sentAt = sentAt;
        return notification;
    }

    /**
     * Mark notification as sent.
     */
    public void markSent(String messageId) {
        if (!(status instanceof NotificationStatus.Pending)) {
            throw new IllegalStateException("Can only send pending notifications");
        }
        this.status = new NotificationStatus.Sent(messageId);
        this.messageId = messageId;
        this.sentAt = Instant.now();
    }

    /**
     * Mark notification as failed.
     */
    public void markFailed(String reason) {
        this.status = new NotificationStatus.Failed(reason);
        this.failureReason = reason;
    }

    /**
     * Mark notification as delivered.
     */
    public void markDelivered() {
        if (!(status instanceof NotificationStatus.Sent)) {
            throw new IllegalStateException("Can only mark sent notifications as delivered");
        }
        this.status = new NotificationStatus.Delivered();
    }

    // Getters
    public NotificationId getId() {
        return id;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
