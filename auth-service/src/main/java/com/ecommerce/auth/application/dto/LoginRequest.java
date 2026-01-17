package com.ecommerce.auth.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login request DTO.
 * 
 * Using Java 21 record for immutability.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
public record LoginRequest(
        @NotBlank(message = "Username is required") String username,

        @NotBlank(message = "Password is required") String password) {
}
