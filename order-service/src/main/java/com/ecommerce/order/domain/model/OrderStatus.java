package com.ecommerce.order.domain.model;

import com.ecommerce.common.exception.ErrorCode;

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
            case Pending s -> "PENDING";
            case Confirmed s -> "CONFIRMED";
            case Paid s -> "PAID";
            case Shipped s -> "SHIPPED";
            case Delivered s -> "DELIVERED";
            case Cancelled s -> "CANCELLED";
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
            default -> throw new IllegalArgumentException(ErrorCode.INVALID_VALUE.formatMessage("order status", value));
        };
    }

    /**
     * Check if order can be cancelled.
     */
    default boolean canBeCancelled() {
        return switch (this) {
            case Pending s -> true;
            case Confirmed s -> true;
            case Paid s -> false;
            case Shipped s -> false;
            case Delivered s -> false;
            case Cancelled s -> false;
        };
    }

    /**
     * Check if order is terminal (no more state changes).
     */
    default boolean isTerminal() {
        return switch (this) {
            case Delivered s -> true;
            case Cancelled s -> true;
            default -> false;
        };
    }
}
