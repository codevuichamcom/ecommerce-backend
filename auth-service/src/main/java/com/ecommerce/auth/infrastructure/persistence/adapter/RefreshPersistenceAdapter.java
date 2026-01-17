package com.ecommerce.auth.infrastructure.persistence.adapter;

import com.ecommerce.auth.domain.model.RefreshToken;
import com.ecommerce.auth.domain.model.UserId;
import com.ecommerce.auth.domain.repository.RefreshTokenRepository;
import com.ecommerce.auth.infrastructure.persistence.entity.RefreshTokenEntity;
import com.ecommerce.auth.infrastructure.persistence.repository.JpaRefreshTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter for RefreshToken.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Component
public class RefreshPersistenceAdapter implements RefreshTokenRepository {

    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;

    public RefreshPersistenceAdapter(JpaRefreshTokenRepository jpaRefreshTokenRepository) {
        this.jpaRefreshTokenRepository = jpaRefreshTokenRepository;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        RefreshTokenEntity entity = toEntity(token);
        RefreshTokenEntity savedEntity = jpaRefreshTokenRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRefreshTokenRepository.findByToken(token).map(this::toDomain);
    }

    @Override
    public Optional<RefreshToken> findById(UUID id) {
        return jpaRefreshTokenRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional
    public void deleteByUserId(UserId userId) {
        jpaRefreshTokenRepository.deleteByUserId(userId.value());
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        jpaRefreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
    }

    private RefreshTokenEntity toEntity(RefreshToken token) {
        return new RefreshTokenEntity(
                token.getToken(),
                token.getUserId().value(),
                token.getExpiresAt());
    }

    private RefreshToken toDomain(RefreshTokenEntity entity) {
        return new RefreshToken(
                entity.getId(),
                new UserId(entity.getUserId()),
                entity.getToken(),
                entity.getExpiryDate(),
                false // default since DB doesn't have revoked column yet
        );
    }
}
