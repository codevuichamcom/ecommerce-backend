package com.ecommerce.order.domain.model;

import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.exception.ValidationException;
import com.ecommerce.common.domain.ValueObject;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Money value object for orders.
 */
public record Money(BigDecimal amount, String currency) implements ValueObject {

    public static final String DEFAULT_CURRENCY = "USD";

    public Money {
        if (amount == null) {
            throw new ValidationException(ErrorCode.NOT_NULL, "Amount");
        }
        if (currency == null) {
            throw new ValidationException(ErrorCode.NOT_NULL, "Currency");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(ErrorCode.NEGATIVE_AMOUNT);
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount, DEFAULT_CURRENCY);
    }

    public static Money of(double amount) {
        return new Money(BigDecimal.valueOf(amount), DEFAULT_CURRENCY);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO, DEFAULT_CURRENCY);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new ValidationException(ErrorCode.CURRENCY_MISMATCH);
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }
}
