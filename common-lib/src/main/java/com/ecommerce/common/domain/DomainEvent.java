package com.ecommerce.common.domain;

import java.time.Instant;

/**
 * Marker interface for Domain Events.
 * All domain events should implement this interface.
 * 
 * Uses Java 21 pattern: Events are typically implemented as records.
 */
public interface DomainEvent {

    /**
     * Unique identifier for this event instance.
     */
    String eventId();

    /**
     * Timestamp when this event occurred.
     */
    Instant occurredAt();

    /**
     * Name of the event type for serialization/routing.
     */
    default String eventType() {
        return this.getClass().getSimpleName();
    }
}
