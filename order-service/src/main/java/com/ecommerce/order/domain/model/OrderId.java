package com.ecommerce.order.domain.model;

import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.exception.ValidationException;
import com.ecommerce.common.domain.ValueObject;

/**
 * Order identifier value object.
 */
public record OrderId(String value) implements ValueObject {

    public OrderId {
        if (value == null) {
            throw new ValidationException(ErrorCode.NOT_NULL, "Order ID");
        }
        if (value.isBlank()) {
            throw new ValidationException(ErrorCode.NOT_BLANK, "Order ID");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
