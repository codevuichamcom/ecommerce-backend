package com.ecommerce.order.application.dto;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Order response DTO.
 */
public record OrderResponse(
        String id,
        String customerId,
        List<OrderItemResponse> items,
        String status,
        BigDecimal totalAmount,
        String currency,
        int totalItems,
        Instant createdAt,
        Instant updatedAt) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId().value(),
                order.getCustomerId().value(),
                order.getItems().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                order.getStatus().toDbValue(),
                order.getTotalAmount().amount(),
                order.getTotalAmount().currency(),
                order.getTotalItems(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }

    public record OrderItemResponse(
            String productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal) {
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(
                    item.productId(),
                    item.productName(),
                    item.quantity(),
                    item.unitPrice().amount(),
                    item.subtotal().amount());
        }

    }
}
