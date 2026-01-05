package com.ecommerce.order.domain.event;

import com.ecommerce.common.domain.DomainEvent;

import java.time.Instant;

/**
 * Event raised when an order is cancelled.
 */
public record OrderCancelled(
        String eventId,
        Instant occurredAt,
        String orderId,
        String customerId,
        String reason) implements DomainEvent {
}
