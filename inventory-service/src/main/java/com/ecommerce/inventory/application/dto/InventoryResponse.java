package com.ecommerce.inventory.application.dto;

import com.ecommerce.inventory.domain.model.Inventory;

import java.time.Instant;

/**
 * Inventory response DTO.
 */
public record InventoryResponse(
        String id,
        String productId,
        int availableQuantity,
        int reservedQuantity,
        int totalQuantity,
        Instant createdAt,
        Instant updatedAt) {

    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId().value(),
                inventory.getProductId(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                inventory.getTotalQuantity(),
                inventory.getCreatedAt(),
                inventory.getUpdatedAt());
    }
}
