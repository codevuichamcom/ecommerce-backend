package com.ecommerce.order.domain.model;

import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.exception.ValidationException;

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
        if (productId == null) {
            throw new ValidationException(ErrorCode.NOT_NULL, "Product ID");
        }
        if (productName == null) {
            throw new ValidationException(ErrorCode.NOT_NULL, "Product name");
        }
        if (unitPrice == null) {
            throw new ValidationException(ErrorCode.NOT_NULL, "Unit price");
        }
        if (quantity <= 0) {
            throw new ValidationException(ErrorCode.INVALID_VALUE, "Quantity", "positive");
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
