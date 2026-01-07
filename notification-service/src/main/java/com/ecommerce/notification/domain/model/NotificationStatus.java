package com.ecommerce.notification.domain.model;

/**
 * Status of a notification.
 */
public sealed interface NotificationStatus {

    /**
     * Notification is pending to be sent.
     */
    record Pending() implements NotificationStatus {
        public static final String NAME = "PENDING";
    }

    /**
     * Notification was sent successfully.
     */
    record Sent(String messageId) implements NotificationStatus {
        public static final String NAME = "SENT";
    }

    /**
     * Notification delivery was confirmed.
     */
    record Delivered() implements NotificationStatus {
        public static final String NAME = "DELIVERED";
    }

    /**
     * Notification sending failed.
     */
    record Failed(String reason) implements NotificationStatus {
        public static final String NAME = "FAILED";
    }

    /**
     * Get status name for persistence.
     */
    default String name() {
        return switch (this) {
            case Pending p -> Pending.NAME;
            case Sent s -> Sent.NAME;
            case Delivered d -> Delivered.NAME;
            case Failed f -> Failed.NAME;
        };
    }
}
