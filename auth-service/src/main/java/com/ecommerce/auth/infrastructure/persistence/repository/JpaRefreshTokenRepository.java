package com.ecommerce.auth.infrastructure.persistence.repository;

import com.ecommerce.auth.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for RefreshTokenEntity.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Repository
public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByToken(String token);

    void deleteByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.expiryDate < :now")
    void deleteByExpiryDateBefore(Instant now);
}
