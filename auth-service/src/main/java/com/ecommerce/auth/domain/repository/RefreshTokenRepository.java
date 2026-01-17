package com.ecommerce.auth.domain.repository;

import com.ecommerce.auth.domain.model.RefreshToken;
import com.ecommerce.auth.domain.model.UserId;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository port interface for RefreshToken entity.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findById(UUID id);

    void deleteByUserId(UserId userId);

    void deleteExpiredTokens();
}
