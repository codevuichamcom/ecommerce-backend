package com.ecommerce.order.infrastructure.persistence.adapter;

import com.ecommerce.order.domain.model.*;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.order.infrastructure.persistence.mapper.OrderMapper;
import com.ecommerce.order.infrastructure.persistence.repository.OrderJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adapter implementing OrderRepository port.
 */
@Component
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderMapper orderMapper;

    public OrderPersistenceAdapter(OrderJpaRepository jpaRepository, OrderMapper orderMapper) {
        this.jpaRepository = jpaRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    public Order save(Order order) {
        var entity = orderMapper.toJpaEntity(order);
        var saved = jpaRepository.save(java.util.Objects.requireNonNull(entity));
        return orderMapper.toDomainEntity(saved);
    }

    @Override
    public Order saveAndFlush(Order order) {
        var entity = orderMapper.toJpaEntity(order);
        var saved = jpaRepository.saveAndFlush(java.util.Objects.requireNonNull(entity));
        return orderMapper.toDomainEntity(saved);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpaRepository.findByIdWithItems(Objects.requireNonNull(id.value()))
                .map(orderMapper::toDomainEntity);
    }

    @Override
    public Optional<Order> findByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey)
                .map(orderMapper::toDomainEntity);
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return jpaRepository.findByCustomerIdWithItems(customerId).stream()
                .map(orderMapper::toDomainEntity)
                .toList();
    }

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.existsByIdempotencyKey(idempotencyKey);
    }
}
