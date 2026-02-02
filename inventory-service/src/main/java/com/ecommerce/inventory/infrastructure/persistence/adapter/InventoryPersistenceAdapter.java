package com.ecommerce.inventory.infrastructure.persistence.adapter;

import com.ecommerce.inventory.domain.model.Inventory;
import com.ecommerce.inventory.domain.model.InventoryId;
import com.ecommerce.inventory.domain.repository.InventoryRepository;
import com.ecommerce.inventory.infrastructure.persistence.entity.InventoryJpaEntity;
import com.ecommerce.inventory.infrastructure.persistence.mapper.InventoryMapper;
import com.ecommerce.inventory.infrastructure.persistence.repository.InventoryJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Adapter implementing InventoryRepository port.
 */
@Component
public class InventoryPersistenceAdapter implements InventoryRepository {

    private final InventoryJpaRepository jpaRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryPersistenceAdapter(InventoryJpaRepository jpaRepository, InventoryMapper inventoryMapper) {
        this.jpaRepository = jpaRepository;
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    public Inventory save(Inventory inventory) {
        InventoryJpaEntity entity = inventoryMapper.toJpaEntity(inventory);
        return inventoryMapper.toDomainEntity(jpaRepository.save(java.util.Objects.requireNonNull(entity)));
    }

    @Override
    public Optional<Inventory> findById(InventoryId id) {
        return jpaRepository.findById(Objects.requireNonNull(id.value()))
                .map(inventoryMapper::toDomainEntity);
    }

    @Override
    public Optional<Inventory> findByProductId(String productId) {
        return jpaRepository.findByProductId(productId)
                .map(inventoryMapper::toDomainEntity);
    }

    @Override
    public boolean existsByProductId(String productId) {
        return jpaRepository.existsByProductId(productId);
    }
}
