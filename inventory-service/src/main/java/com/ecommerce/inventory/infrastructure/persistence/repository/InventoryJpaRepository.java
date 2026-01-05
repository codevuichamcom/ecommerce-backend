package com.ecommerce.inventory.infrastructure.persistence.repository;

import com.ecommerce.inventory.infrastructure.persistence.entity.InventoryJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data JPA repository for Inventory.
 */
public interface InventoryJpaRepository extends JpaRepository<InventoryJpaEntity, String> {

    Optional<InventoryJpaEntity> findByProductId(String productId);

    boolean existsByProductId(String productId);

    /**
     * Find with pessimistic lock for critical operations.
     * Use this only when optimistic locking is not enough.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryJpaEntity i WHERE i.productId = :productId")
    Optional<InventoryJpaEntity> findByProductIdForUpdate(@Param("productId") String productId);
}
