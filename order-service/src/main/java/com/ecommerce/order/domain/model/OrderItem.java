package com.ecommerce.order.domain.model;

import java.math.BigDecimal;
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
            int quantity, BigDecimal unitPrice, String currency) {
        Money price = new Money(unitPrice, currency);
        return new OrderItem(productId, productName, quantity, price, price.multiply(quantity));
    }

    // Getters for compatibility with old code if needed (records use fieldName())
    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public Money getSubtotal() {
        return subtotal;
    }
}
