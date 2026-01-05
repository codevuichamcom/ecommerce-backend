package com.ecommerce.order.domain.event;

import com.ecommerce.common.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event raised when an order is created.
 */
public record OrderCreated(
        String eventId,
        Instant occurredAt,
        String orderId,
        String customerId,
        int itemCount,
        BigDecimal totalAmount) implements DomainEvent {
}
