package com.ecommerce.inventory.infrastructure.persistence.repository;

import com.ecommerce.inventory.infrastructure.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;

/**
 * Spring Data JPA repository for Outbox events.
 * This is an infrastructure detail and stays in the infrastructure layer.
 */
public interface InventoryOutboxJpaRepository extends JpaRepository<OutboxEventEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM OutboxEventEntity e WHERE e.published = false ORDER BY e.createdAt ASC LIMIT :limit")
    List<OutboxEventEntity> findUnpublishedForUpdate(@Param("limit") int limit);

    @Modifying
    @Query("DELETE FROM OutboxEventEntity e WHERE e.published = true AND e.publishedAt < :cutoff")
    void deletePublishedBefore(@Param("cutoff") Instant cutoff);
}
