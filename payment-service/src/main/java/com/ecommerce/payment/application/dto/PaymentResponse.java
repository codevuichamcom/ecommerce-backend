package com.ecommerce.payment.application.dto;

import com.ecommerce.payment.domain.model.Payment;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payment response DTO.
 */
public record PaymentResponse(
        String id,
        String orderId,
        String customerId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String status,
        String transactionId,
        String failureReason,
        Instant createdAt,
        Instant completedAt) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId().value(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod().name(),
                payment.getStatus().name(),
                payment.getTransactionId(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getCompletedAt());
    }
}
