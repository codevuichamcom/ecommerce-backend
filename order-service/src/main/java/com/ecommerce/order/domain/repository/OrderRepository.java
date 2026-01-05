package com.ecommerce.order.domain.repository;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * Order repository interface (port).
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId id);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    List<Order> findByCustomerId(String customerId);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
