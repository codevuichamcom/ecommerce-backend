package com.ecommerce.common.events;

import com.ecommerce.common.domain.DomainEvent;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Payment-related domain events.
 * These events are published when payment processing state changes occur.
 */
@JsonSubTypes({
        @JsonSubTypes.Type(value = PaymentEvents.PaymentRequested.class, name = "PaymentRequested"),
        @JsonSubTypes.Type(value = PaymentEvents.PaymentCompleted.class, name = "PaymentCompleted"),
        @JsonSubTypes.Type(value = PaymentEvents.PaymentFailed.class, name = "PaymentFailed"),
        @JsonSubTypes.Type(value = PaymentEvents.PaymentRefunded.class, name = "PaymentRefunded")
})
public sealed interface PaymentEvents extends DomainEvent {

    /**
     * ID of the payment this event relates to.
     */
    String paymentId();

    /**
     * ID of the order this payment is for.
     */
    String orderId();

    // ==================== Payment Requested ====================

    /**
     * Published by order-service when payment is requested.
     */
    record PaymentRequested(
            String eventId,
            Instant occurredAt,
            String paymentId,
            String orderId,
            String customerId,
            BigDecimal amount,
            String currency) implements PaymentEvents {

        public static PaymentRequested create(
                String orderId,
                String customerId,
                BigDecimal amount,
                String currency) {
            return new PaymentRequested(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    UUID.randomUUID().toString(), // New payment ID
                    orderId,
                    customerId,
                    amount,
                    currency);
        }
    }

    // ==================== Payment Completed ====================

    /**
     * Published when payment is successfully processed.
     */
    record PaymentCompleted(
            String eventId,
            Instant occurredAt,
            String paymentId,
            String orderId,
            String customerId,
            BigDecimal amount,
            String currency,
            String transactionId) implements PaymentEvents {

        public static PaymentCompleted create(
                String paymentId,
                String orderId,
                String customerId,
                BigDecimal amount,
                String currency,
                String transactionId) {
            return new PaymentCompleted(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    paymentId,
                    orderId,
                    customerId,
                    amount,
                    currency,
                    transactionId);
        }
    }

    // ==================== Payment Failed ====================

    /**
     * Published when payment processing fails.
     */
    record PaymentFailed(
            String eventId,
            Instant occurredAt,
            String paymentId,
            String orderId,
            String customerId,
            String reason,
            String errorCode) implements PaymentEvents {

        public static PaymentFailed create(
                String paymentId,
                String orderId,
                String customerId,
                String reason,
                String errorCode) {
            return new PaymentFailed(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    paymentId,
                    orderId,
                    customerId,
                    reason,
                    errorCode);
        }
    }

    // ==================== Payment Refunded ====================

    /**
     * Published when a payment is refunded.
     */
    record PaymentRefunded(
            String eventId,
            Instant occurredAt,
            String paymentId,
            String orderId,
            BigDecimal amount,
            String reason) implements PaymentEvents {

        public static PaymentRefunded create(
                String paymentId,
                String orderId,
                BigDecimal amount,
                String reason) {
            return new PaymentRefunded(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    paymentId,
                    orderId,
                    amount,
                    reason);
        }
    }
}
