package com.ecommerce.inventory.infrastructure.persistence.mapper;

import com.ecommerce.inventory.domain.model.Inventory;
import com.ecommerce.inventory.domain.model.InventoryId;
import com.ecommerce.inventory.infrastructure.persistence.entity.InventoryJpaEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Mapper between Inventory domain model and JPA entities.
 */
@Component
public class InventoryMapper {

    public InventoryJpaEntity toJpaEntity(Inventory inventory) {
        var entity = new InventoryJpaEntity();
        entity.setId(inventory.getId().value());
        entity.setProductId(inventory.getProductId());
        entity.setAvailableQuantity(inventory.getAvailableQuantity());
        entity.setReservedQuantity(inventory.getReservedQuantity());
        entity.setVersion(inventory.getVersion());
        entity.setCreatedAt(inventory.getCreatedAt());
        entity.setUpdatedAt(inventory.getUpdatedAt());
        return entity;
    }

    public Inventory toDomainEntity(@NonNull InventoryJpaEntity entity) {
        return Inventory.reconstitute(
                new InventoryId(entity.getId()),
                entity.getProductId(),
                entity.getAvailableQuantity(),
                entity.getReservedQuantity(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
