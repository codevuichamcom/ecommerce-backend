package com.ecommerce.inventory.domain.event;

import com.ecommerce.common.domain.DomainEvent;

import java.time.Instant;

/**
 * Event raised when stock is reserved for an order.
 */
public record StockReserved(
        String eventId,
        Instant occurredAt,
        String inventoryId,
        String productId,
        int quantity,
        String reservationReference) implements DomainEvent {
}
