package com.ecommerce.payment.infrastructure.persistence.repository;

import com.ecommerce.payment.infrastructure.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data JPA repository for outbox events.
 */
@Repository
public interface OutboxJpaRepository extends JpaRepository<OutboxEventEntity, String> {

    @Query("SELECT o FROM OutboxEventEntity o WHERE o.published = false ORDER BY o.createdAt ASC LIMIT :limit")
    List<OutboxEventEntity> findUnpublishedOrderByCreatedAt(@Param("limit") int limit);

    @Modifying
    @Query("UPDATE OutboxEventEntity o SET o.published = true, o.publishedAt = :publishedAt WHERE o.id = :id")
    void markAsPublished(@Param("id") String id, @Param("publishedAt") Instant publishedAt);

    @Modifying
    @Query("DELETE FROM OutboxEventEntity o WHERE o.published = true AND o.publishedAt < :threshold")
    void deletePublishedOlderThan(@Param("threshold") Instant threshold);
}
