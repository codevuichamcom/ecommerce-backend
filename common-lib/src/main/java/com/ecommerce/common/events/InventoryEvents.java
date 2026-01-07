package com.ecommerce.common.events;

import com.ecommerce.common.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Inventory-related domain events.
 * These events are published when inventory state changes occur.
 */
public sealed interface InventoryEvents extends DomainEvent {

    /**
     * ID of the product this event relates to.
     */
    String productId();

    // ==================== Stock Reserved ====================

    /**
     * Published when stock is successfully reserved for an order.
     */
    record StockReserved(
            String eventId,
            Instant occurredAt,
            String productId,
            String orderId,
            int quantity,
            int remainingAvailable) implements InventoryEvents {

        public static StockReserved create(
                String productId,
                String orderId,
                int quantity,
                int remainingAvailable) {
            return new StockReserved(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    productId,
                    orderId,
                    quantity,
                    remainingAvailable);
        }
    }

    // ==================== Stock Reservation Failed ====================

    /**
     * Published when stock reservation fails (insufficient stock).
     */
    record StockReservationFailed(
            String eventId,
            Instant occurredAt,
            String productId,
            String orderId,
            int requestedQuantity,
            int availableQuantity,
            String reason) implements InventoryEvents {

        public static StockReservationFailed create(
                String productId,
                String orderId,
                int requestedQuantity,
                int availableQuantity) {
            return new StockReservationFailed(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    productId,
                    orderId,
                    requestedQuantity,
                    availableQuantity,
                    "Insufficient stock");
        }
    }

    // ==================== Stock Released ====================

    /**
     * Published when reserved stock is released (order cancelled/failed).
     */
    record StockReleased(
            String eventId,
            Instant occurredAt,
            String productId,
            String orderId,
            int quantity,
            int newAvailable) implements InventoryEvents {

        public static StockReleased create(
                String productId,
                String orderId,
                int quantity,
                int newAvailable) {
            return new StockReleased(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    productId,
                    orderId,
                    quantity,
                    newAvailable);
        }
    }

    // ==================== All Items Reserved ====================

    /**
     * Published when all items in an order have been successfully reserved.
     * This is a composite event that indicates the order can proceed to payment.
     */
    record AllItemsReserved(
            String eventId,
            Instant occurredAt,
            String productId, // Can be null for composite events
            String orderId) implements InventoryEvents {

        public static AllItemsReserved create(String orderId) {
            return new AllItemsReserved(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    null,
                    orderId);
        }
    }
}
