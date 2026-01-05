package com.ecommerce.inventory.domain.model;

import com.ecommerce.common.domain.ValueObject;

import java.util.Objects;

/**
 * Inventory identifier value object.
 */
public record InventoryId(String value) implements ValueObject {

    public InventoryId {
        Objects.requireNonNull(value, "Inventory ID must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Inventory ID must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
