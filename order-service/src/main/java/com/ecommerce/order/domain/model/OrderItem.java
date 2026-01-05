package com.ecommerce.order.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Order item entity (part of Order aggregate).
 */
public class OrderItem {

    private final String productId;
    private final String productName;
    private final int quantity;
    private final Money unitPrice;
    private final Money subtotal;

    private OrderItem(String productId, String productName, int quantity, Money unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = unitPrice.multiply(quantity);
    }

    public static OrderItem create(String productId, String productName, int quantity, Money unitPrice) {
        Objects.requireNonNull(productId, "Product ID must not be null");
        Objects.requireNonNull(productName, "Product name must not be null");
        Objects.requireNonNull(unitPrice, "Unit price must not be null");

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        return new OrderItem(productId, productName, quantity, unitPrice);
    }

    public static OrderItem reconstitute(String productId, String productName,
            int quantity, BigDecimal unitPrice, String currency) {
        return new OrderItem(productId, productName, quantity, new Money(unitPrice, currency));
    }

    // Getters
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
