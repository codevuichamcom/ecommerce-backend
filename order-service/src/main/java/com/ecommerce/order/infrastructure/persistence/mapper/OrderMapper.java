package com.ecommerce.order.infrastructure.persistence.mapper;

import com.ecommerce.order.domain.model.*;
import com.ecommerce.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between Order domain model and JPA entities.
 */
@Component
public class OrderMapper {

    public OrderJpaEntity toJpaEntity(Order order) {
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
            itemEntity.setProductId(item.productId());
            itemEntity.setProductName(item.productName());
            itemEntity.setQuantity(item.quantity());
            itemEntity.setUnitPrice(item.unitPrice().amount());
            itemEntity.setCurrency(item.unitPrice().currency());
            itemEntity.setSubtotal(item.subtotal().amount());

            entity.addItem(itemEntity);
        }

        return entity;
    }

    public Order toDomainEntity(OrderJpaEntity entity) {
        var items = entity.getItems().stream()
                .map(itemEntity -> OrderItem.reconstitute(
                        itemEntity.getProductId(),
                        itemEntity.getProductName(),
                        itemEntity.getQuantity(),
                        new Money(itemEntity.getUnitPrice(), itemEntity.getCurrency()),
                        new Money(itemEntity.getSubtotal(), itemEntity.getCurrency())))
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
