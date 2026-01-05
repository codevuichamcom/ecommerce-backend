package com.ecommerce.inventory.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Command to reserve stock.
 */
public record ReserveStockCommand(
        @NotBlank(message = "Product ID is required") String productId,

        @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be at least 1") Integer quantity,

        @NotBlank(message = "Reservation reference is required") String reservationReference) {
}
