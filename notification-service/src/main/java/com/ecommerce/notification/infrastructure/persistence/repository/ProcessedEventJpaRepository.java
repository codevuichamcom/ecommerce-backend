package com.ecommerce.notification.infrastructure.persistence.repository;

import com.ecommerce.notification.infrastructure.persistence.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * Spring Data JPA repository for processed events (idempotency tracking).
 */
@Repository
public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, String> {

    boolean existsByEventId(String eventId);

    @Modifying
    @Query("DELETE FROM ProcessedEventEntity p WHERE p.processedAt < :threshold")
    void deleteOlderThan(@Param("threshold") Instant threshold);
}
