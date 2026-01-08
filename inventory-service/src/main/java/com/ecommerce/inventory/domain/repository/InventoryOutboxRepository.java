package com.ecommerce.inventory.domain.repository;

import com.ecommerce.common.outbox.OutboxMessage;
import com.ecommerce.inventory.infrastructure.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryOutboxRepository extends JpaRepository<OutboxEventEntity, String> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT e FROM OutboxEventEntity e WHERE e.published = false ORDER BY e.createdAt ASC LIMIT :limit")
    List<OutboxEventEntity> findUnpublishedForUpdate(
            @org.springframework.data.repository.query.Param("limit") int limit);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM OutboxEventEntity e WHERE e.published = true AND e.publishedAt < :cutoff")
    void deletePublishedBefore(@org.springframework.data.repository.query.Param("cutoff") java.time.Instant cutoff);
}
