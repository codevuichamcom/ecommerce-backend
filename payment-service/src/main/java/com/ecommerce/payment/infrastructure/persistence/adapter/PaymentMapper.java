package com.ecommerce.payment.infrastructure.persistence.adapter;

import com.ecommerce.payment.domain.model.*;
import com.ecommerce.payment.infrastructure.persistence.entity.PaymentJpaEntity;

/**
 * Mapper between Payment domain model and JPA entity.
 */
public final class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentJpaEntity toEntity(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setId(payment.getId().value());
        entity.setOrderId(payment.getOrderId());
        entity.setCustomerId(payment.getCustomerId());
        entity.setAmount(payment.getAmount());
        entity.setCurrency(payment.getCurrency());
        entity.setPaymentMethod(payment.getPaymentMethod().name());
        entity.setStatus(payment.getStatus().name());
        entity.setTransactionId(payment.getTransactionId());
        entity.setFailureReason(payment.getFailureReason());
        entity.setFailureCode(payment.getFailureCode());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());
        entity.setCompletedAt(payment.getCompletedAt());
        return entity;
    }

    public static Payment toDomain(PaymentJpaEntity entity) {
        return Payment.reconstitute(
                new PaymentId(entity.getId()),
                entity.getOrderId(),
                entity.getCustomerId(),
                entity.getAmount(),
                entity.getCurrency(),
                PaymentMethod.valueOf(entity.getPaymentMethod()),
                mapStatus(entity),
                entity.getTransactionId(),
                entity.getFailureReason(),
                entity.getFailureCode(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCompletedAt());
    }

    private static PaymentStatus mapStatus(PaymentJpaEntity entity) {
        return switch (entity.getStatus()) {
            case "PENDING" -> new PaymentStatus.Pending();
            case "PROCESSING" -> new PaymentStatus.Processing();
            case "COMPLETED" -> new PaymentStatus.Completed(entity.getTransactionId());
            case "FAILED" -> new PaymentStatus.Failed(
                    entity.getFailureReason() != null ? entity.getFailureReason() : "Unknown",
                    entity.getFailureCode());
            case "REFUNDED" -> new PaymentStatus.Refunded(entity.getTransactionId(), null);
            default -> throw new IllegalStateException("Unknown payment status: " + entity.getStatus());
        };
    }
}
