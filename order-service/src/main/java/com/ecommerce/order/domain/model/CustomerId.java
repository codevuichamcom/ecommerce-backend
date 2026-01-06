package com.ecommerce.order.domain.model;

import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.exception.ValidationException;
import com.ecommerce.common.domain.ValueObject;

/**
 * Customer identifier value object.
 */
public record CustomerId(String value) implements ValueObject {

    public CustomerId {
        if (value == null) {
            throw new ValidationException(ErrorCode.NOT_NULL, "Customer ID");
        }
        if (value.isBlank()) {
            throw new ValidationException(ErrorCode.NOT_BLANK, "Customer ID");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
