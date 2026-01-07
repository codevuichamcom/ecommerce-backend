package com.ecommerce.notification.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA entity for Notification.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationJpaEntity {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "recipient_id", nullable = false, length = 50)
    private String recipientId;

    @Column(name = "recipient_email", length = 255)
    private String recipientEmail;

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "message_id", length = 100)
    private String messageId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;
}
