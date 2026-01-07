package com.ecommerce.notification.domain.model;

import com.ecommerce.common.domain.ValueObject;

/**
 * Notification ID value object.
 */
public record NotificationId(String value) implements ValueObject {

    public NotificationId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Notification ID cannot be null or blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
