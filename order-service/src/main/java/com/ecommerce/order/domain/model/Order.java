package com.ecommerce.order.domain.model;

import com.ecommerce.common.domain.AggregateRoot;
import com.ecommerce.common.util.IdGenerator;
import com.ecommerce.order.domain.event.OrderCancelled;
import com.ecommerce.order.domain.event.OrderConfirmed;
import com.ecommerce.order.domain.event.OrderCreated;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lombok.Getter;

/**
 * Order aggregate root.
 * Manages order lifecycle and contains order items.
 */
@Getter
public class Order extends AggregateRoot<OrderId> {

    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private Money totalAmount;
    private final String idempotencyKey;
    private final Instant createdAt;
    private Instant updatedAt;

    private Order(OrderId id, CustomerId customerId, List<OrderItem> items,
            OrderStatus status, Money totalAmount, String idempotencyKey,
            Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.status = status;
        this.totalAmount = totalAmount;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     * Create a new order.
     */
    public static Order create(CustomerId customerId, List<OrderItem> items, String idempotencyKey) {
        Objects.requireNonNull(customerId, "Customer ID must not be null");
        Objects.requireNonNull(items, "Items must not be null");

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }

        var orderId = new OrderId(IdGenerator.generate("ord"));
        var totalAmount = calculateTotal(items);

        var order = new Order(
                orderId,
                customerId,
                items,
                OrderStatus.Pending.INSTANCE,
                totalAmount,
                idempotencyKey,
                Instant.now());

        // Register creation event
        order.registerEvent(new OrderCreated(
                IdGenerator.generate(),
                Instant.now(),
                orderId.value(),
                customerId.value(),
                items.size(),
                totalAmount.amount()));

        return order;
    }

    /**
     * Reconstitute from persistence.
     */
    public static Order reconstitute(OrderId id, CustomerId customerId, List<OrderItem> items,
            OrderStatus status, Money totalAmount, String idempotencyKey,
            Instant createdAt, Instant updatedAt) {
        var order = new Order(id, customerId, items, status, totalAmount, idempotencyKey, createdAt);
        order.updatedAt = updatedAt;
        return order;
    }

    /**
     * Confirm the order (inventory reserved successfully).
     */
    public void confirm() {
        if (!(status instanceof OrderStatus.Pending)) {
            throw new IllegalStateException("Can only confirm pending orders");
        }

        this.status = OrderStatus.Confirmed.INSTANCE;
        this.updatedAt = Instant.now();

        registerEvent(new OrderConfirmed(
                IdGenerator.generate(),
                Instant.now(),
                id.value(),
                customerId.value()));
    }

    /**
     * Cancel the order.
     */
    public void cancel(String reason) {
        if (!status.canBeCancelled()) {
            throw new IllegalStateException(
                    String.format("Cannot cancel order in status: %s", status.toDbValue()));
        }

        this.status = new OrderStatus.Cancelled(reason);
        this.updatedAt = Instant.now();

        registerEvent(new OrderCancelled(
                IdGenerator.generate(),
                Instant.now(),
                id.value(),
                customerId.value(),
                reason));
    }

    /**
     * Mark as paid.
     */
    public void markAsPaid() {
        if (!(status instanceof OrderStatus.Confirmed)) {
            throw new IllegalStateException("Can only mark confirmed orders as paid");
        }

        this.status = OrderStatus.Paid.INSTANCE;
        this.updatedAt = Instant.now();
    }

    private static Money calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(Money.zero(), Money::add);
    }

    // Custom business getters
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public int getTotalItems() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }
}
