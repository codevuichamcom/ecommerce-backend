package com.ecommerce.inventory.domain.model;

import com.ecommerce.common.domain.ValueObject;

import java.util.Objects;

/**
 * Stock quantity value object with validation.
 */
public record StockQuantity(int value) implements ValueObject {

    public StockQuantity {
        if (value < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative: " + value);
        }
    }

    public static StockQuantity of(int value) {
        return new StockQuantity(value);
    }

    public static StockQuantity zero() {
        return new StockQuantity(0);
    }

    public StockQuantity add(StockQuantity other) {
        Objects.requireNonNull(other, "Cannot add null quantity");
        return new StockQuantity(this.value + other.value);
    }

    public StockQuantity subtract(StockQuantity other) {
        Objects.requireNonNull(other, "Cannot subtract null quantity");
        int result = this.value - other.value;
        if (result < 0) {
            throw new IllegalArgumentException(
                    String.format("Cannot subtract %d from %d", other.value, this.value));
        }
        return new StockQuantity(result);
    }

    public boolean isGreaterThanOrEqual(StockQuantity other) {
        return this.value >= other.value;
    }

    public boolean isZero() {
        return value == 0;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
