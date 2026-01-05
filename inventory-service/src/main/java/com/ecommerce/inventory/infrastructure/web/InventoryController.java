package com.ecommerce.inventory.infrastructure.web;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.inventory.application.dto.*;
import com.ecommerce.inventory.application.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Inventory operations.
 */
@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Stock management APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @Operation(summary = "Create inventory for a product")
    public ResponseEntity<ApiResponse<InventoryResponse>> createInventory(
            @Valid @RequestBody CreateInventoryCommand command) {
        var inventory = inventoryService.createInventory(command);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(inventory));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory by product ID")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryByProductId(
            @PathVariable String productId) {
        var inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock for an order")
    public ResponseEntity<ApiResponse<StockOperationResponse>> reserveStock(
            @Valid @RequestBody ReserveStockCommand command) {
        var result = inventoryService.reserveStock(command);

        if (result.success()) {
            return ResponseEntity.ok(ApiResponse.success(result));
        } else {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("STOCK_OPERATION_FAILED", result.message(), result));
        }
    }

    @PostMapping("/release")
    @Operation(summary = "Release reserved stock")
    public ResponseEntity<ApiResponse<StockOperationResponse>> releaseStock(
            @Valid @RequestBody ReleaseStockCommand command) {
        var result = inventoryService.releaseStock(command);

        if (result.success()) {
            return ResponseEntity.ok(ApiResponse.success(result));
        } else {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("STOCK_OPERATION_FAILED", result.message(), result));
        }
    }

    @PostMapping("/product/{productId}/add")
    @Operation(summary = "Add stock (replenishment)")
    public ResponseEntity<ApiResponse<InventoryResponse>> addStock(
            @PathVariable String productId,
            @RequestParam int quantity) {
        var inventory = inventoryService.addStock(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }
}
