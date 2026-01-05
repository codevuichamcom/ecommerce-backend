package com.ecommerce.product.application.dto;

import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Product response DTO.
 * Uses Java 21 record for immutability.
 */
public record ProductResponse(
        String id,
        String name,
        String description,
        String sku,
        BigDecimal price,
        String currency,
        ProductStatus status,
        boolean available,
        Instant createdAt,
        Instant updatedAt) {

    /**
     * Map from domain entity to response DTO.
     */
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId().value(),
                product.getName(),
                product.getDescription(),
                product.getSku(),
                product.getPrice().amount(),
                product.getPrice().currency().getCurrencyCode(),
                product.getStatus(),
                product.isAvailable(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
