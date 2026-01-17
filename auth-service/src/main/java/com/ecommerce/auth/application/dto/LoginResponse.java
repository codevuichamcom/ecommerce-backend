package com.ecommerce.auth.application.dto;

import com.ecommerce.auth.domain.model.Role;

import java.util.Set;

/**
 * Login response DTO containing JWT tokens and user info.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String userId,
        String username,
        String email,
        Set<Role> roles,
        long expiresIn // seconds
) {
}
