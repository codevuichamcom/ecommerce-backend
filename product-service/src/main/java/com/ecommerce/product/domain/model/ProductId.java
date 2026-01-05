package com.ecommerce.product.domain.model;

import com.ecommerce.common.domain.ValueObject;

import java.util.Objects;

/**
 * Product identifier value object.
 * Uses Java 21 record for immutability.
 */
public record ProductId(String value) implements ValueObject {

    public ProductId {
        Objects.requireNonNull(value, "Product ID must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Product ID must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
