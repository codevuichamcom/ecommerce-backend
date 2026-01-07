package com.ecommerce.order.domain.saga;

/**
 * States for the Order Saga orchestration.
 */
public enum SagaState {
    STARTED,
    INVENTORY_RESERVED,
    INVENTORY_FAILED,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    COMPENSATING,
    COMPENSATED,
    COMPLETED,
    FAILED
}
