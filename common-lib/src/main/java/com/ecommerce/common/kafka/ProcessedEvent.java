package com.ecommerce.common.kafka;

import java.time.Instant;

/**
 * Represents a processed event for idempotency tracking.
 * Each service should have a table to store these.
 */
public record ProcessedEvent(
        String eventId,
        String eventType,
        Instant processedAt) {
    public static ProcessedEvent create(String eventId, String eventType) {
        return new ProcessedEvent(eventId, eventType, Instant.now());
    }
}
