package com.ecommerce.order.domain.model;

/**
 * Order status using Java 21 sealed interface.
 * Enables exhaustive pattern matching in business logic.
 */
public sealed interface OrderStatus {

    /**
     * Order created but not yet processed.
     */
    record Pending() implements OrderStatus {
        public static final Pending INSTANCE = new Pending();
    }

    /**
     * Inventory reserved, awaiting payment.
     */
    record Confirmed() implements OrderStatus {
        public static final Confirmed INSTANCE = new Confirmed();
    }

    /**
     * Payment received, ready for shipment.
     */
    record Paid() implements OrderStatus {
        public static final Paid INSTANCE = new Paid();
    }

    /**
     * Order shipped to customer.
     */
    record Shipped() implements OrderStatus {
        public static final Shipped INSTANCE = new Shipped();
    }

    /**
     * Order delivered to customer.
     */
    record Delivered() implements OrderStatus {
        public static final Delivered INSTANCE = new Delivered();
    }

    /**
     * Order cancelled.
     */
    record Cancelled(String reason) implements OrderStatus {
    }

    /**
     * Get string representation for persistence.
     */
    default String toDbValue() {
        return switch (this) {
            case Pending _ -> "PENDING";
            case Confirmed _ -> "CONFIRMED";
            case Paid _ -> "PAID";
            case Shipped _ -> "SHIPPED";
            case Delivered _ -> "DELIVERED";
            case Cancelled _ -> "CANCELLED";
        };
    }

    /**
     * Create from database value.
     */
    static OrderStatus fromDbValue(String value, String cancelReason) {
        return switch (value) {
            case "PENDING" -> Pending.INSTANCE;
            case "CONFIRMED" -> Confirmed.INSTANCE;
            case "PAID" -> Paid.INSTANCE;
            case "SHIPPED" -> Shipped.INSTANCE;
            case "DELIVERED" -> Delivered.INSTANCE;
            case "CANCELLED" -> new Cancelled(cancelReason);
            default -> throw new IllegalArgumentException("Unknown order status: " + value);
        };
    }

    /**
     * Check if order can be cancelled.
     */
    default boolean canBeCancelled() {
        return switch (this) {
            case Pending _,Confirmed _ -> true;
            case Paid _,Shipped _,Delivered _,Cancelled _ -> false;
        };
    }

    /**
     * Check if order is terminal (no more state changes).
     */
    default boolean isTerminal() {
        return switch (this) {
            case Delivered _,Cancelled _ -> true;
            default -> false;
        };
    }
}
