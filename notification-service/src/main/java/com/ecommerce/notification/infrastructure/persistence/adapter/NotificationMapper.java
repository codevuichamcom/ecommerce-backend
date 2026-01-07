package com.ecommerce.notification.infrastructure.persistence.adapter;

import com.ecommerce.notification.domain.model.*;
import com.ecommerce.notification.infrastructure.persistence.entity.NotificationJpaEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Mapper between Notification domain model and JPA entity.
 */
public final class NotificationMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private NotificationMapper() {
    }

    public static NotificationJpaEntity toEntity(Notification notification) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(notification.getId().value());
        entity.setRecipientId(notification.getRecipientId());
        entity.setRecipientEmail(notification.getRecipientEmail());
        entity.setType(notification.getType().name());
        entity.setChannel(notification.getChannel().name());
        entity.setSubject(notification.getSubject());
        entity.setMessage(notification.getMessage());
        entity.setMetadata(serializeMetadata(notification.getMetadata()));
        entity.setStatus(notification.getStatus().name());
        entity.setMessageId(notification.getMessageId());
        entity.setFailureReason(notification.getFailureReason());
        entity.setCreatedAt(notification.getCreatedAt());
        entity.setSentAt(notification.getSentAt());
        return entity;
    }

    public static Notification toDomain(NotificationJpaEntity entity) {
        return Notification.reconstitute(
                new NotificationId(entity.getId()),
                entity.getRecipientId(),
                entity.getRecipientEmail(),
                NotificationType.valueOf(entity.getType()),
                NotificationChannel.valueOf(entity.getChannel()),
                entity.getSubject(),
                entity.getMessage(),
                deserializeMetadata(entity.getMetadata()),
                mapStatus(entity),
                entity.getMessageId(),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getSentAt());
    }

    private static NotificationStatus mapStatus(NotificationJpaEntity entity) {
        return switch (entity.getStatus()) {
            case "PENDING" -> new NotificationStatus.Pending();
            case "SENT" -> new NotificationStatus.Sent(entity.getMessageId());
            case "DELIVERED" -> new NotificationStatus.Delivered();
            case "FAILED" -> new NotificationStatus.Failed(
                    entity.getFailureReason() != null ? entity.getFailureReason() : "Unknown");
            default -> throw new IllegalStateException("Unknown notification status: " + entity.getStatus());
        };
    }

    private static String serializeMetadata(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static Map<String, String> deserializeMetadata(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(metadata, new TypeReference<Map<String, String>>() {
            });
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }
}
