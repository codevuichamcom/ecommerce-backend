package com.ecommerce.order.domain.saga;

import com.ecommerce.order.domain.model.OrderId;

import java.util.Optional;

/**
 * Port interface for OrderSaga persistence.
 */
public interface OrderSagaRepository {
    OrderSaga save(OrderSaga saga);

    Optional<OrderSaga> findById(OrderId orderId);
}
