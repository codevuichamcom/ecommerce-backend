package com.ecommerce.product.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Command to create a new product.
 * Uses Java 21 record with validation annotations.
 */
public record CreateProductCommand(
        @NotBlank(message = "Name is required") @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters") String name,

        @Size(max = 2000, message = "Description must not exceed 2000 characters") String description,

        @NotBlank(message = "SKU is required") @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters") String sku,

        @NotNull(message = "Price is required") @DecimalMin(value = "0.01", message = "Price must be greater than 0") BigDecimal price) {
}
