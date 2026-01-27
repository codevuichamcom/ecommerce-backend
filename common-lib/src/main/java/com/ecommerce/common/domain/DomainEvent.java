package com.ecommerce.common.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;

/**
 * Marker interface for Domain Events.
 * All domain events should implement this interface.
 * 
 * Uses Jackson polymorphic type info for robust event detection.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "eventType", visible = true)
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
    @JsonProperty("eventType")
    default String eventType() {
        return this.getClass().getSimpleName();
    }
}
