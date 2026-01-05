package com.ecommerce.order.domain.model;

import com.ecommerce.common.domain.ValueObject;

import java.util.Objects;

/**
 * Customer identifier value object.
 */
public record CustomerId(String value) implements ValueObject {

    public CustomerId {
        Objects.requireNonNull(value, "Customer ID must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Customer ID must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
