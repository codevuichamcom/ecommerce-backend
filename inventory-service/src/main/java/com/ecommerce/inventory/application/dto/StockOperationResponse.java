package com.ecommerce.inventory.application.dto;

/**
 * Stock operation response DTO.
 */
public record StockOperationResponse(
        boolean success,
        String inventoryId,
        String productId,
        int availableQuantity,
        int reservedQuantity,
        String message) {

    public static StockOperationResponse success(String inventoryId, String productId,
            int available, int reserved) {
        return new StockOperationResponse(
                true, inventoryId, productId, available, reserved, "Operation successful");
    }

    public static StockOperationResponse failure(String inventoryId, String productId,
            int available, int reserved, String message) {
        return new StockOperationResponse(
                false, inventoryId, productId, available, reserved, message);
    }
}
