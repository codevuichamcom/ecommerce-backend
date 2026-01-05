package com.ecommerce.inventory.infrastructure.persistence.adapter;

import com.ecommerce.inventory.domain.model.Inventory;
import com.ecommerce.inventory.domain.model.InventoryId;
import com.ecommerce.inventory.domain.repository.InventoryRepository;
import com.ecommerce.inventory.infrastructure.persistence.entity.InventoryJpaEntity;
import com.ecommerce.inventory.infrastructure.persistence.repository.InventoryJpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Adapter implementing InventoryRepository port.
 */
@Component
public class InventoryPersistenceAdapter implements InventoryRepository {

    private final InventoryJpaRepository jpaRepository;

    public InventoryPersistenceAdapter(InventoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @SuppressWarnings("null")
    public Inventory save(Inventory inventory) {
        InventoryJpaEntity entity = toJpaEntity(inventory);
        return toDomainEntity(jpaRepository.save(entity));
    }

    @Override
    public Optional<Inventory> findById(InventoryId id) {
        return jpaRepository.findById(Objects.requireNonNull(id.value()))
                .map(this::toDomainEntity);
    }

    @Override
    public Optional<Inventory> findByProductId(String productId) {
        return jpaRepository.findByProductId(productId)
                .map(this::toDomainEntity);
    }

    @Override
    public boolean existsByProductId(String productId) {
        return jpaRepository.existsByProductId(productId);
    }

    private InventoryJpaEntity toJpaEntity(Inventory inventory) {
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

    private Inventory toDomainEntity(@NonNull InventoryJpaEntity entity) {
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
