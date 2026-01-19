package com.ecommerce.auth.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Refresh token request DTO.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required") String refreshToken) {
}
