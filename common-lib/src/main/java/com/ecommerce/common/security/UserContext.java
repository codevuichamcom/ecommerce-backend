package com.ecommerce.common.security;

import java.util.Set;

/**
 * Record holding user information extracted from request headers.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
public record UserContext(
        String userId,
        String username,
        Set<Role> roles) {
    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role);
    }

    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }
}
