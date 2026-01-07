package com.ecommerce.order.domain.saga;

import com.ecommerce.common.domain.AggregateRoot;
import com.ecommerce.order.domain.model.OrderId;

import java.time.Instant;

/**
 * OrderSaga aggregate root.
 * Tracks the state of the distributed transaction for an order.
 */
public class OrderSaga extends AggregateRoot<OrderId> {

    private final OrderId orderId;
    private SagaState state;
    private String lastError;
    private final Instant createdAt;
    private Instant updatedAt;

    private OrderSaga(OrderId orderId, SagaState state, Instant createdAt) {
        this.orderId = orderId;
        this.state = state;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static OrderSaga start(OrderId orderId) {
        return new OrderSaga(orderId, SagaState.STARTED, Instant.now());
    }

    public static OrderSaga reconstitute(OrderId orderId, SagaState state, String lastError, Instant createdAt,
            Instant updatedAt) {
        OrderSaga saga = new OrderSaga(orderId, state, createdAt);
        saga.lastError = lastError;
        saga.updatedAt = updatedAt;
        return saga;
    }

    public void inventoryReserved() {
        if (state != SagaState.STARTED) {
            throw new IllegalStateException("Cannot move to INVENTORY_RESERVED from " + state);
        }
        this.state = SagaState.INVENTORY_RESERVED;
        this.updatedAt = Instant.now();
    }

    public void inventoryFailed(String reason) {
        if (state != SagaState.STARTED) {
            throw new IllegalStateException("Cannot move to INVENTORY_FAILED from " + state);
        }
        this.state = SagaState.INVENTORY_FAILED;
        this.lastError = reason;
        this.updatedAt = Instant.now();
    }

    public void paymentCompleted() {
        if (state != SagaState.INVENTORY_RESERVED) {
            throw new IllegalStateException("Cannot move to PAYMENT_COMPLETED from " + state);
        }
        this.state = SagaState.PAYMENT_COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void paymentFailed(String reason) {
        if (state != SagaState.INVENTORY_RESERVED) {
            throw new IllegalStateException("Cannot move to PAYMENT_FAILED from " + state);
        }
        this.state = SagaState.PAYMENT_FAILED;
        this.lastError = reason;
        this.updatedAt = Instant.now();
    }

    public void startCompensating() {
        if (state != SagaState.PAYMENT_FAILED && state != SagaState.INVENTORY_FAILED) {
            // We might also allow compensating from other states if needed
        }
        this.state = SagaState.COMPENSATING;
        this.updatedAt = Instant.now();
    }

    public void compensated() {
        if (state != SagaState.COMPENSATING) {
            throw new IllegalStateException("Cannot move to COMPENSATED from " + state);
        }
        this.state = SagaState.COMPENSATED;
        this.updatedAt = Instant.now();
    }

    public void complete() {
        if (state != SagaState.PAYMENT_COMPLETED) {
            throw new IllegalStateException("Cannot move to COMPLETED from " + state);
        }
        this.state = SagaState.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void fail(String reason) {
        this.state = SagaState.FAILED;
        this.lastError = reason;
        this.updatedAt = Instant.now();
    }

    @Override
    public OrderId getId() {
        return orderId;
    }

    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }

    public SagaState getState() {
        return state;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
