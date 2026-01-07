package com.ecommerce.payment.domain.model;

import com.ecommerce.common.domain.ValueObject;

/**
 * Payment ID value object.
 */
public record PaymentId(String value) implements ValueObject {

    public PaymentId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Payment ID cannot be null or blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
