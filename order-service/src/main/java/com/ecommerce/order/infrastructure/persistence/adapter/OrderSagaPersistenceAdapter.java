package com.ecommerce.order.infrastructure.persistence.adapter;

import com.ecommerce.order.domain.model.OrderId;
import com.ecommerce.order.domain.saga.OrderSaga;
import com.ecommerce.order.domain.saga.OrderSagaRepository;
import com.ecommerce.order.infrastructure.persistence.entity.OrderSagaJpaEntity;
import com.ecommerce.order.infrastructure.persistence.repository.OrderSagaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderSagaPersistenceAdapter implements OrderSagaRepository {

    private final OrderSagaJpaRepository repository;

    public OrderSagaPersistenceAdapter(OrderSagaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @SuppressWarnings("null")
    public OrderSaga save(OrderSaga saga) {
        OrderSagaJpaEntity entity = toEntity(saga);
        OrderSagaJpaEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    @SuppressWarnings("null")
    public Optional<OrderSaga> findById(OrderId orderId) {
        return repository.findById(orderId.value())
                .map(this::toDomain);
    }

    private OrderSagaJpaEntity toEntity(OrderSaga saga) {
        OrderSagaJpaEntity entity = new OrderSagaJpaEntity();
        entity.setOrderId(saga.getId().value());
        entity.setState(saga.getState());
        entity.setLastError(saga.getLastError());
        entity.setCreatedAt(saga.getCreatedAt());
        entity.setUpdatedAt(saga.getUpdatedAt());
        return entity;
    }

    private OrderSaga toDomain(OrderSagaJpaEntity entity) {
        return OrderSaga.reconstitute(
                new OrderId(entity.getOrderId()),
                entity.getState(),
                entity.getLastError(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
