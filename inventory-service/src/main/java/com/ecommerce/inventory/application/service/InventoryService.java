package com.ecommerce.inventory.application.service;

import com.ecommerce.common.exception.ConflictException;
import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.inventory.application.dto.*;
import com.ecommerce.inventory.domain.model.Inventory;
import com.ecommerce.inventory.domain.model.InventoryId;
import com.ecommerce.inventory.domain.model.StockOperationResult;
import com.ecommerce.inventory.domain.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inventory application service.
 * Handles stock operations with optimistic locking and retry.
 */
@Service
@Transactional
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Create inventory for a product.
     */
    public InventoryResponse createInventory(CreateInventoryCommand command) {
        // Check for existing inventory
        if (inventoryRepository.existsByProductId(command.productId())) {
            throw ConflictException.duplicate("Inventory", command.productId());
        }

        var inventory = Inventory.create(command.productId(), command.quantity());
        var saved = inventoryRepository.save(inventory);

        log.info("Created inventory {} for product {} with quantity {}",
                saved.getId(), command.productId(), command.quantity());

        return InventoryResponse.from(saved);
    }

    /**
     * Get inventory by product ID.
     */
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(String productId) {
        var inventory = findByProductIdOrThrow(productId);
        return InventoryResponse.from(inventory);
    }

    /**
     * Reserve stock for an order.
     * Uses @Retryable for optimistic lock conflicts.
     */
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    public StockOperationResponse reserveStock(ReserveStockCommand command) {
        log.debug("Reserving {} units for product {} (ref: {})",
                command.quantity(), command.productId(), command.reservationReference());

        var inventory = findByProductIdOrThrow(command.productId());

        var result = inventory.reserve(command.quantity(), command.reservationReference());

        // Pattern matching with sealed interface
        return switch (result) {
            case StockOperationResult.Success s -> {
                inventoryRepository.save(inventory);
                log.info("Reserved {} units for product {} (ref: {})",
                        command.quantity(), command.productId(), command.reservationReference());
                yield StockOperationResponse.success(
                        s.inventoryId(), command.productId(),
                        s.availableQuantity(), s.reservedQuantity());
            }
            case StockOperationResult.InsufficientStock is -> {
                log.warn("Insufficient stock for product {}: requested={}, available={}",
                        command.productId(), is.requested(), is.available());
                yield StockOperationResponse.failure(
                        is.inventoryId(), command.productId(),
                        is.available(), 0,
                        String.format("Insufficient stock: requested %d, available %d",
                                is.requested(), is.available()));
            }
            case StockOperationResult.AlreadyReserved ar -> {
                log.warn("Stock already reserved for product {}: {}",
                        command.productId(), ar.existingReservationId());
                yield StockOperationResponse.failure(
                        ar.inventoryId(), command.productId(),
                        0, 0,
                        "Stock already reserved: " + ar.existingReservationId());
            }
            case StockOperationResult.ReservationNotFound rnf -> {
                // This shouldn't happen for reserve, but handle exhaustively
                yield StockOperationResponse.failure(
                        rnf.inventoryId(), command.productId(),
                        0, 0,
                        "Unexpected error during reservation");
            }
        };
    }

    /**
     * Release reserved stock.
     * Uses @Retryable for optimistic lock conflicts.
     */
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    public StockOperationResponse releaseStock(ReleaseStockCommand command) {
        log.debug("Releasing {} units for product {} (ref: {})",
                command.quantity(), command.productId(), command.reservationReference());

        var inventory = findByProductIdOrThrow(command.productId());

        var result = inventory.release(command.quantity(), command.reservationReference());

        return switch (result) {
            case StockOperationResult.Success s -> {
                inventoryRepository.save(inventory);
                log.info("Released {} units for product {} (ref: {})",
                        command.quantity(), command.productId(), command.reservationReference());
                yield StockOperationResponse.success(
                        s.inventoryId(), command.productId(),
                        s.availableQuantity(), s.reservedQuantity());
            }
            case StockOperationResult.ReservationNotFound rnf -> {
                log.warn("Reservation not found for product {}: {}",
                        command.productId(), rnf.reservationId());
                yield StockOperationResponse.failure(
                        rnf.inventoryId(), command.productId(),
                        0, 0,
                        "Reservation not found: " + rnf.reservationId());
            }
            default -> StockOperationResponse.failure(
                    inventory.getId().value(), command.productId(),
                    0, 0,
                    "Unexpected error during release");
        };
    }

    /**
     * Add stock (replenishment).
     */
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    public InventoryResponse addStock(String productId, int quantity) {
        var inventory = findByProductIdOrThrow(productId);
        inventory.addStock(quantity);
        var saved = inventoryRepository.save(inventory);

        log.info("Added {} units to product {}", quantity, productId);

        return InventoryResponse.from(saved);
    }

    private Inventory findByProductIdOrThrow(String productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Inventory", productId));
    }
}
