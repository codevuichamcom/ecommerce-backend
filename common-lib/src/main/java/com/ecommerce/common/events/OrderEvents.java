package com.ecommerce.common.events;

import com.ecommerce.common.domain.DomainEvent;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Order-related domain events.
 * These events are published when order state changes occur.
 */
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderEvents.OrderCreated.class, name = "OrderCreated"),
        @JsonSubTypes.Type(value = OrderEvents.OrderConfirmed.class, name = "OrderConfirmed"),
        @JsonSubTypes.Type(value = OrderEvents.OrderCancelled.class, name = "OrderCancelled")
})
public sealed interface OrderEvents extends DomainEvent {

    /**
     * ID of the order aggregate this event relates to.
     */
    String orderId();

    // ==================== Order Created ====================

    /**
     * Published when a new order is created and enters PENDING status.
     */
    record OrderCreated(
            String eventId,
            Instant occurredAt,
            String orderId,
            String customerId,
            List<OrderItemData> items,
            BigDecimal totalAmount,
            String currency,
            String idempotencyKey) implements OrderEvents {

        public static OrderCreated create(
                String orderId,
                String customerId,
                List<OrderItemData> items,
                BigDecimal totalAmount,
                String currency,
                String idempotencyKey) {
            return new OrderCreated(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    orderId,
                    customerId,
                    items,
                    totalAmount,
                    currency,
                    idempotencyKey);
        }
    }

    // ==================== Order Confirmed ====================

    /**
     * Published when an order is fully confirmed (payment successful).
     */
    record OrderConfirmed(
            String eventId,
            Instant occurredAt,
            String orderId,
            String customerId,
            BigDecimal totalAmount,
            String currency) implements OrderEvents {

        public static OrderConfirmed create(String orderId, String customerId, BigDecimal totalAmount,
                String currency) {
            return new OrderConfirmed(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    orderId,
                    customerId,
                    totalAmount,
                    currency);
        }
    }

    // ==================== Order Cancelled ====================

    /**
     * Published when an order is cancelled.
     */
    record OrderCancelled(
            String eventId,
            Instant occurredAt,
            String orderId,
            String customerId,
            List<OrderItemData> items,
            String reason,
            boolean requiresRefund) implements OrderEvents {

        public static OrderCancelled create(String orderId, String customerId, List<OrderItemData> items, String reason,
                boolean requiresRefund) {
            return new OrderCancelled(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    orderId,
                    customerId,
                    items,
                    reason,
                    requiresRefund);
        }
    }

    // ==================== Supporting Records ====================

    /**
     * Order item data for events (denormalized for event consumers).
     */
    record OrderItemData(
            String productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            String currency) {
    }
}
