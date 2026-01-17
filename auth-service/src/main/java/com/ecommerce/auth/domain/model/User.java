package com.ecommerce.auth.domain.model;

import com.ecommerce.common.security.Role;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * User aggregate root - Domain model for authentication and authorization.
 * 
 * This is a pure domain model, separate from JPA entity.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
public class User {

    private final UserId id;
    private String username;
    private String email;
    private String passwordHash;
    private Set<Role> roles;
    private boolean enabled;
    private Instant createdAt;
    private Instant lastLoginAt;

    // Constructor for new user
    public User(UserId id, String username, String email, String passwordHash, Set<Role> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = new HashSet<>(roles);
        this.enabled = true;
        this.createdAt = Instant.now();
    }

    // Constructor for existing user (from repository)
    public User(UserId id, String username, String email, String passwordHash,
            Set<Role> roles, boolean enabled, Instant createdAt, Instant lastLoginAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = new HashSet<>(roles);
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    // Business methods
    public void updateLastLogin() {
        this.lastLoginAt = Instant.now();
    }

    public void disable() {
        this.enabled = false;
    }

    public void enable() {
        this.enabled = true;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public boolean hasRole(Role role) {
        return this.roles.contains(role);
    }

    // Getters
    public UserId getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Set<Role> getRoles() {
        return new HashSet<>(roles);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }
}
