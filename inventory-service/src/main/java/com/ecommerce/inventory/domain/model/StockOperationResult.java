package com.ecommerce.inventory.domain.model;

/**
 * Result of a stock operation using Java 21 sealed interface.
 * Enables exhaustive pattern matching in switch expressions.
 */
public sealed interface StockOperationResult {

    /**
     * Operation succeeded.
     */
    record Success(String inventoryId, int availableQuantity, int reservedQuantity)
            implements StockOperationResult {
    }

    /**
     * Not enough stock available.
     */
    record InsufficientStock(String inventoryId, int requested, int available)
            implements StockOperationResult {
    }

    /**
     * Stock reservation already exists for this order.
     */
    record AlreadyReserved(String inventoryId, String existingReservationId)
            implements StockOperationResult {
    }

    /**
     * No reservation found to release/confirm.
     */
    record ReservationNotFound(String inventoryId, String reservationId)
            implements StockOperationResult {
    }

    /**
     * Check if operation was successful.
     */
    default boolean isSuccess() {
        return this instanceof Success;
    }
}
