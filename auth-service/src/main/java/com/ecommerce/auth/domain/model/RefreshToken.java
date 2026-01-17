package com.ecommerce.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Refresh Token entity for JWT token refresh flow.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
public class RefreshToken {

    private final UUID id;
    private final UserId userId;
    private final String token;
    private final Instant expiresAt;
    private boolean revoked;

    public RefreshToken(UUID id, UserId userId, String token, Instant expiresAt) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.revoked = false;
    }

    public RefreshToken(UUID id, UserId userId, String token, Instant expiresAt, boolean revoked) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public static RefreshToken create(UserId userId, String token, Instant expiresAt) {
        return new RefreshToken(UUID.randomUUID(), userId, token, expiresAt);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }
}
