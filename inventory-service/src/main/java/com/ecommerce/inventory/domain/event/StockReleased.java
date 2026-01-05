package com.ecommerce.inventory.domain.event;

import com.ecommerce.common.domain.DomainEvent;

import java.time.Instant;

/**
 * Event raised when reserved stock is released (e.g., order cancelled).
 */
public record StockReleased(
        String eventId,
        Instant occurredAt,
        String inventoryId,
        String productId,
        int quantity,
        String reservationReference) implements DomainEvent {
}
