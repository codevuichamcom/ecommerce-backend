package com.ecommerce.product.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Command to update product details.
 * All fields are optional - only provided fields will be updated.
 */
public record UpdateProductCommand(
        @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters") String name,

        @Size(max = 2000, message = "Description must not exceed 2000 characters") String description,

        @DecimalMin(value = "0.01", message = "Price must be greater than 0") BigDecimal price) {
}
