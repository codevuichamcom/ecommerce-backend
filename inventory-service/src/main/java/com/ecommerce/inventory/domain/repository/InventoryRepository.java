package com.ecommerce.inventory.domain.repository;

import com.ecommerce.inventory.domain.model.Inventory;
import com.ecommerce.inventory.domain.model.InventoryId;

import java.util.Optional;

/**
 * Inventory repository interface (port).
 */
public interface InventoryRepository {

    /**
     * Save inventory.
     */
    Inventory save(Inventory inventory);

    /**
     * Find inventory by ID.
     */
    Optional<Inventory> findById(InventoryId id);

    /**
     * Find inventory by product ID.
     */
    Optional<Inventory> findByProductId(String productId);

    /**
     * Check if inventory exists for product.
     */
    boolean existsByProductId(String productId);
}
