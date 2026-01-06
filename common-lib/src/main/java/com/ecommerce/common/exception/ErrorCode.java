package com.ecommerce.common.exception;

import lombok.Getter;

/**
 * Enumeration of business error codes and their default messages.
 */
@Getter
public enum ErrorCode {
    // General Errors
    INTERNAL_ERROR("GEN_001", "An unexpected error occurred"),
    INVALID_ARGUMENT("GEN_002", "Invalid argument provided"),
    NOT_FOUND("GEN_003", "%s with id '%s' not found"),
    VALIDATION_ERROR("GEN_004", "Validation failed"),
    CONFLICT("GEN_005", "Conflict occurred"),
    NOT_NULL("GEN_006", "%s must not be null"),
    NOT_BLANK("GEN_007", "%s must not be blank"),
    INVALID_VALUE("GEN_008", "%s must be %s"),

    // Order Errors
    ORDER_NOT_FOUND("ORD_001", "Order not found"),
    ORDER_CANNOT_BE_CANCELLED("ORD_002", "Order in status %s cannot be cancelled"),
    ORDER_INVALID_CONFIRMATION("ORD_003", "Can only confirm pending orders"),
    ORDER_INVALID_PAID_STATUS("ORD_004", "Can only mark confirmed orders as paid"),
    ORDER_ITEMS_EMPTY("ORD_005", "Order must have at least one item"),

    // Inventory Errors
    INSUFFICIENT_STOCK("INV_001", "Insufficient stock for product %s: requested %d, available %d"),
    INVENTORY_SERVICE_UNAVAILABLE("INV_002", "Inventory service is currently unavailable: %s"),

    // Product Errors
    PRODUCT_NOT_AVAILABLE("PRD_001", "Product '%s' is not available"),

    // Money/Currency Errors
    NEGATIVE_AMOUNT("MON_001", "Amount must be non-negative"),
    CURRENCY_MISMATCH("MON_002", "Currency mismatch");

    private final String code;
    private final String messageTemplate;

    ErrorCode(String code, String messageTemplate) {
        this.code = code;
        this.messageTemplate = messageTemplate;
    }

    public String formatMessage(Object... args) {
        return String.format(messageTemplate, args);
    }
}
