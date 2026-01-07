package com.ecommerce.payment.domain.model;

import com.ecommerce.common.domain.AggregateRoot;
import com.github.f4b6a3.ulid.UlidCreator;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payment aggregate root.
 * Represents a payment for an order.
 */
public class Payment extends AggregateRoot<PaymentId> {

    private final PaymentId id;
    private final String orderId;
    private final String customerId;
    private final BigDecimal amount;
    private final String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionId;
    private String failureReason;
    private String failureCode;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    private Payment(
            PaymentId id,
            String orderId,
            String customerId,
            BigDecimal amount,
            String currency,
            PaymentMethod paymentMethod,
            PaymentStatus status,
            Instant createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     * Create a new payment for an order.
     */
    public static Payment create(
            String orderId,
            String customerId,
            BigDecimal amount,
            String currency) {
        return create(orderId, customerId, amount, currency, PaymentMethod.CREDIT_CARD);
    }

    /**
     * Create a new payment for an order with specific payment method.
     */
    public static Payment create(
            String orderId,
            String customerId,
            BigDecimal amount,
            String currency,
            PaymentMethod paymentMethod) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID is required");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }

        return new Payment(
                new PaymentId(UlidCreator.getUlid().toString()),
                orderId,
                customerId,
                amount,
                currency,
                paymentMethod,
                new PaymentStatus.Pending(),
                Instant.now());
    }

    /**
     * Reconstitute a payment from persistence.
     */
    public static Payment reconstitute(
            PaymentId id,
            String orderId,
            String customerId,
            BigDecimal amount,
            String currency,
            PaymentMethod paymentMethod,
            PaymentStatus status,
            String transactionId,
            String failureReason,
            String failureCode,
            Instant createdAt,
            Instant updatedAt,
            Instant completedAt) {
        Payment payment = new Payment(id, orderId, customerId, amount, currency, paymentMethod, status, createdAt);
        payment.transactionId = transactionId;
        payment.failureReason = failureReason;
        payment.failureCode = failureCode;
        payment.updatedAt = updatedAt;
        payment.completedAt = completedAt;
        return payment;
    }

    /**
     * Start processing the payment.
     */
    public void startProcessing() {
        if (!(status instanceof PaymentStatus.Pending)) {
            throw new IllegalStateException("Can only start processing pending payments");
        }
        this.status = new PaymentStatus.Processing();
        this.updatedAt = Instant.now();
    }

    /**
     * Mark payment as completed.
     */
    public void complete(String transactionId) {
        if (!(status instanceof PaymentStatus.Processing)) {
            throw new IllegalStateException("Can only complete processing payments");
        }
        this.status = new PaymentStatus.Completed(transactionId);
        this.transactionId = transactionId;
        this.updatedAt = Instant.now();
        this.completedAt = Instant.now();
    }

    /**
     * Mark payment as failed.
     */
    public void fail(String reason, String errorCode) {
        if (!(status instanceof PaymentStatus.Pending) && !(status instanceof PaymentStatus.Processing)) {
            throw new IllegalStateException("Can only fail pending or processing payments");
        }
        this.status = new PaymentStatus.Failed(reason, errorCode);
        this.failureReason = reason;
        this.failureCode = errorCode;
        this.updatedAt = Instant.now();
    }

    /**
     * Refund the payment.
     */
    public void refund(String refundTransactionId) {
        if (!(status instanceof PaymentStatus.Completed completed)) {
            throw new IllegalStateException("Can only refund completed payments");
        }
        this.status = new PaymentStatus.Refunded(completed.transactionId(), refundTransactionId);
        this.updatedAt = Instant.now();
    }

    // Implement abstract methods from AggregateRoot
    @Override
    public PaymentId getId() {
        return id;
    }

    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }

    // Other getters
    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    /**
     * Check if payment is in a terminal state.
     */
    public boolean isTerminal() {
        return status.isTerminal();
    }
}
