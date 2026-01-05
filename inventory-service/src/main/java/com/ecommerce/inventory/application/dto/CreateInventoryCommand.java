package com.ecommerce.inventory.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Command to create inventory for a product.
 */
public record CreateInventoryCommand(
        @NotBlank(message = "Product ID is required") String productId,

        @NotNull(message = "Quantity is required") @Min(value = 0, message = "Quantity must be non-negative") Integer quantity) {
}
