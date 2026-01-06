package com.ecommerce.order.domain.model;

import java.util.Objects;

/**
 * Order item value object (part of Order aggregate).
 */
public record OrderItem(
        String productId,
        String productName,
        int quantity,
        Money unitPrice,
        Money subtotal) {
    public OrderItem {
        Objects.requireNonNull(productId, "Product ID must not be null");
        Objects.requireNonNull(productName, "Product name must not be null");
        Objects.requireNonNull(unitPrice, "Unit price must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public static OrderItem create(String productId, String productName, int quantity, Money unitPrice) {
        return new OrderItem(productId, productName, quantity, unitPrice, unitPrice.multiply(quantity));
    }

    public static OrderItem reconstitute(String productId, String productName,
            int quantity, Money unitPrice, Money subtotal) {
        return new OrderItem(productId, productName, quantity, unitPrice, subtotal);
    }
}
