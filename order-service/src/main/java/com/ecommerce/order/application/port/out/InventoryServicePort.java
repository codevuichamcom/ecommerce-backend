package com.ecommerce.order.application.port.out;

/**
 * Port for communicating with Inventory service.
 */
public interface InventoryServicePort {

    /**
     * Reserve stock for an order.
     */
    ReservationResult reserveStock(String productId, int quantity, String reservationReference);

    /**
     * Release reserved stock.
     */
    void releaseStock(String productId, int quantity, String reservationReference);

    /**
     * Result of stock reservation.
     */
    sealed interface ReservationResult {
        record Success(int availableQuantity, int reservedQuantity) implements ReservationResult {
        }

        record InsufficientStock(int requested, int available) implements ReservationResult {
        }

        record ServiceUnavailable(String message) implements ReservationResult {
        }

        record Pending(String taskId) implements ReservationResult {
        }
    }
}
