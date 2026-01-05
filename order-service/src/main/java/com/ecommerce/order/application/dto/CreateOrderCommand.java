package com.ecommerce.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Command to create a new order.
 */
public record CreateOrderCommand(
        @NotBlank(message = "Customer ID is required") String customerId,

        @NotEmpty(message = "Order must have at least one item") @Valid List<OrderItemRequest> items) {
}
