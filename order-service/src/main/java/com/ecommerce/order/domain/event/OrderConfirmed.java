package com.ecommerce.order.domain.event;

import com.ecommerce.common.domain.DomainEvent;

import java.time.Instant;

/**
 * Event raised when an order is confirmed.
 */
public record OrderConfirmed(
        String eventId,
        Instant occurredAt,
        String orderId,
        String customerId) implements DomainEvent {
}
