package com.ecommerce.common.outbox;

import java.time.Instant;

/**
 * Represents an event stored in the outbox table.
 * This is a simple record that can be used across services.
 * Each service will have its own JPA entity extending this concept.
 */
public record OutboxMessage(
        String id,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload,
        Instant createdAt,
        boolean published,
        Instant publishedAt) {
    /**
     * Creates a new outbox message (not yet published).
     */
    public static OutboxMessage create(
            String id,
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload) {
        return new OutboxMessage(
                id,
                aggregateType,
                aggregateId,
                eventType,
                payload,
                Instant.now(),
                false,
                null);
    }

    /**
     * Marks this message as published.
     */
    public OutboxMessage markPublished() {
        return new OutboxMessage(
                id,
                aggregateType,
                aggregateId,
                eventType,
                payload,
                createdAt,
                true,
                Instant.now());
    }
}
