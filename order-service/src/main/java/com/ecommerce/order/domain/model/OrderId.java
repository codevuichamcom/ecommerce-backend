package com.ecommerce.order.domain.model;

import com.ecommerce.common.domain.ValueObject;

import java.util.Objects;

/**
 * Order identifier value object.
 */
public record OrderId(String value) implements ValueObject {

    public OrderId {
        Objects.requireNonNull(value, "Order ID must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Order ID must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
