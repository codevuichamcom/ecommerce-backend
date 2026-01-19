package com.ecommerce.auth.domain.model;

import java.util.UUID;

/**
 * Value object representing a User ID.
 * 
 * Using record for immutability and built-in equals/hashCode.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
public record UserId(UUID value) {

    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId from(String value) {
        return new UserId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
