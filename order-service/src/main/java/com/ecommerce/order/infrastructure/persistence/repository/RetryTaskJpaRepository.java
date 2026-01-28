package com.ecommerce.order.infrastructure.persistence.repository;

import com.ecommerce.order.infrastructure.persistence.entity.RetryStatus;
import com.ecommerce.order.infrastructure.persistence.entity.RetryTaskEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface RetryTaskJpaRepository extends JpaRepository<RetryTaskEntity, UUID> {

    @Query("SELECT r FROM RetryTaskEntity r WHERE r.status = :status AND r.nextRetryAt <= :now")
    List<RetryTaskEntity> findPendingTasks(@Param("status") RetryStatus status, @Param("now") Instant now,
            Pageable pageable);
}
