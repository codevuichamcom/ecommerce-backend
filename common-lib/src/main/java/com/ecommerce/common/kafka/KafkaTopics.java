package com.ecommerce.common.kafka;

/**
 * Constants for Kafka topic names.
 * Centralized topic naming for all services.
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Utility class
    }

    // ==================== Order Events ====================

    /**
     * Topic for order-related events.
     * Published by: order-service
     * Consumed by: inventory-service, payment-service, notification-service
     */
    public static final String ORDER_EVENTS = "order-events";

    // ==================== Payment Events ====================

    /**
     * Topic for payment-related events.
     * Published by: payment-service
     * Consumed by: order-service, notification-service
     */
    public static final String PAYMENT_EVENTS = "payment-events";

    // ==================== Inventory Events ====================

    /**
     * Topic for inventory-related events.
     * Published by: inventory-service
     * Consumed by: order-service
     */
    public static final String INVENTORY_EVENTS = "inventory-events";

    // ==================== Notification Events ====================

    /**
     * Topic for notification requests.
     * Published by: various services
     * Consumed by: notification-service
     */
    public static final String NOTIFICATION_EVENTS = "notification-events";

    // ==================== Consumer Groups ====================

    public static final String ORDER_SERVICE_GROUP = "order-service-group";
    public static final String PAYMENT_SERVICE_GROUP = "payment-service-group";
    public static final String INVENTORY_SERVICE_GROUP = "inventory-service-group";
    public static final String NOTIFICATION_SERVICE_GROUP = "notification-service-group";
}
