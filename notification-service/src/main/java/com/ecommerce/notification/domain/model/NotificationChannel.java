package com.ecommerce.notification.domain.model;

/**
 * Channel through which notification is sent.
 */
public enum NotificationChannel {
    EMAIL("Email"),
    SMS("SMS"),
    PUSH("Push Notification"),
    IN_APP("In-App Notification");

    private final String displayName;

    NotificationChannel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
