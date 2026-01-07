package com.ecommerce.notification.domain.model;

/**
 * Type of notification.
 */
public enum NotificationType {
    ORDER_CREATED("Order Created", "Your order has been created"),
    ORDER_CONFIRMED("Order Confirmed", "Your order has been confirmed"),
    ORDER_SHIPPED("Order Shipped", "Your order has been shipped"),
    ORDER_DELIVERED("Order Delivered", "Your order has been delivered"),
    ORDER_CANCELLED("Order Cancelled", "Your order has been cancelled"),
    PAYMENT_RECEIVED("Payment Received", "We have received your payment"),
    PAYMENT_FAILED("Payment Failed", "Your payment could not be processed"),
    REFUND_PROCESSED("Refund Processed", "Your refund has been processed");

    private final String title;
    private final String defaultMessage;

    NotificationType(String title, String defaultMessage) {
        this.title = title;
        this.defaultMessage = defaultMessage;
    }

    public String getTitle() {
        return title;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
