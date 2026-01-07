package com.ecommerce.payment.application.dto;

import java.math.BigDecimal;

/**
 * Command to process a payment.
 */
public record ProcessPaymentCommand(
        String orderId,
        String customerId,
        BigDecimal amount,
        String currency) {
    public ProcessPaymentCommand {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID is required");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }
    }
}
