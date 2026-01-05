package com.ecommerce.order.infrastructure.persistence.adapter;

import com.ecommerce.order.domain.model.*;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
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

    public OrderPersistenceAdapter(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @SuppressWarnings("null")
    public Order save(Order order) {
        var entity = toJpaEntity(order);
        var saved = jpaRepository.save(entity);
        return toDomainEntity(saved);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpaRepository.findByIdWithItems(Objects.requireNonNull(id.value()))
                .map(this::toDomainEntity);
    }

    @Override
    public Optional<Order> findByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey)
                .map(this::toDomainEntity);
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return jpaRepository.findByCustomerIdWithItems(customerId).stream()
                .map(this::toDomainEntity)
                .toList();
    }

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.existsByIdempotencyKey(idempotencyKey);
    }

    private OrderJpaEntity toJpaEntity(Order order) {
        var entity = new OrderJpaEntity();
        entity.setId(order.getId().value());
        entity.setCustomerId(order.getCustomerId().value());
        entity.setStatus(order.getStatus().toDbValue());

        if (order.getStatus() instanceof OrderStatus.Cancelled cancelled) {
            entity.setCancelReason(cancelled.reason());
        }

        entity.setTotalAmount(order.getTotalAmount().amount());
        entity.setCurrency(order.getTotalAmount().currency());
        entity.setIdempotencyKey(order.getIdempotencyKey());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());

        // Map items
        for (var item : order.getItems()) {
            var itemEntity = new OrderItemJpaEntity();
            itemEntity.setProductId(item.getProductId());
            itemEntity.setProductName(item.getProductName());
            itemEntity.setQuantity(item.getQuantity());
            itemEntity.setUnitPrice(item.getUnitPrice().amount());
            itemEntity.setCurrency(item.getUnitPrice().currency());
            itemEntity.setSubtotal(item.getSubtotal().amount());
            entity.addItem(itemEntity);
        }

        return entity;
    }

    private Order toDomainEntity(OrderJpaEntity entity) {
        var items = entity.getItems().stream()
                .map(itemEntity -> OrderItem.reconstitute(
                        itemEntity.getProductId(),
                        itemEntity.getProductName(),
                        itemEntity.getQuantity(),
                        itemEntity.getUnitPrice(),
                        itemEntity.getCurrency()))
                .toList();

        var status = OrderStatus.fromDbValue(entity.getStatus(), entity.getCancelReason());

        return Order.reconstitute(
                new OrderId(entity.getId()),
                new CustomerId(entity.getCustomerId()),
                items,
                status,
                new Money(entity.getTotalAmount(), entity.getCurrency()),
                entity.getIdempotencyKey(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
