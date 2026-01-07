package com.ecommerce.payment.domain.model;

/**
 * Payment status using Java 21 sealed interface.
 * Represents all possible states of a payment.
 */
public sealed interface PaymentStatus {

    /**
     * Payment has been created but not yet processed.
     */
    record Pending() implements PaymentStatus {
        public static final String NAME = "PENDING";
    }

    /**
     * Payment is currently being processed.
     */
    record Processing() implements PaymentStatus {
        public static final String NAME = "PROCESSING";
    }

    /**
     * Payment was successfully processed.
     */
    record Completed(String transactionId) implements PaymentStatus {
        public static final String NAME = "COMPLETED";

        public Completed {
            if (transactionId == null || transactionId.isBlank()) {
                throw new IllegalArgumentException("Transaction ID is required for completed payments");
            }
        }
    }

    /**
     * Payment processing failed.
     */
    record Failed(String reason, String errorCode) implements PaymentStatus {
        public static final String NAME = "FAILED";

        public Failed {
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("Reason is required for failed payments");
            }
        }
    }

    /**
     * Payment was refunded.
     */
    record Refunded(String originalTransactionId, String refundTransactionId) implements PaymentStatus {
        public static final String NAME = "REFUNDED";
    }

    /**
     * Get the status name for persistence.
     */
    default String name() {
        return switch (this) {
            case Pending p -> Pending.NAME;
            case Processing p -> Processing.NAME;
            case Completed c -> Completed.NAME;
            case Failed f -> Failed.NAME;
            case Refunded r -> Refunded.NAME;
        };
    }

    /**
     * Check if this is a terminal state.
     */
    default boolean isTerminal() {
        return switch (this) {
            case Completed c -> true;
            case Failed f -> true;
            case Refunded r -> true;
            default -> false;
        };
    }
}
