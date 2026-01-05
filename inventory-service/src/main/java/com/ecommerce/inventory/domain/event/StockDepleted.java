package com.ecommerce.inventory.domain.event;

import com.ecommerce.common.domain.DomainEvent;

import java.time.Instant;

/**
 * Event raised when stock reaches zero.
 */
public record StockDepleted(
        String eventId,
        Instant occurredAt,
        String inventoryId,
        String productId) implements DomainEvent {
}
