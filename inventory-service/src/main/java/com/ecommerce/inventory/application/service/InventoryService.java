package com.ecommerce.inventory.application.service;

import com.ecommerce.common.exception.ConflictException;
import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.inventory.application.dto.*;
import com.ecommerce.inventory.domain.model.Inventory;
import com.ecommerce.inventory.domain.model.StockOperationResult;
import com.ecommerce.inventory.domain.repository.InventoryRepository;
import io.micrometer.core.instrument.MeterRegistry;
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
    private final com.ecommerce.inventory.infrastructure.kafka.InventoryEventProducer eventProducer;
    private final MeterRegistry meterRegistry;

    public InventoryService(InventoryRepository inventoryRepository,
            com.ecommerce.inventory.infrastructure.kafka.InventoryEventProducer eventProducer,
            MeterRegistry meterRegistry) {
        this.inventoryRepository = inventoryRepository;
        this.eventProducer = eventProducer;
        this.meterRegistry = meterRegistry;
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
                meterRegistry.counter("inventory_reservation_total",
                        "product", command.productId(),
                        "status", "success").increment();

                yield StockOperationResponse.success(
                        s.inventoryId(), command.productId(),
                        s.availableQuantity(), s.reservedQuantity());
            }
            case StockOperationResult.InsufficientStock is -> {
                log.warn("Insufficient stock for product {}: requested={}, available={}",
                        command.productId(), is.requested(), is.available());
                meterRegistry.counter("inventory_reservation_total",
                        "product", command.productId(),
                        "status", "insufficient_stock").increment();

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

    /**
     * Handle OrderCreated event.
     * Attempts to reserve stock for all items in the order.
     */
    @Transactional
    public void handleOrderCreated(String orderId, com.fasterxml.jackson.databind.JsonNode itemsNode) {
        log.info("Handling OrderCreated for order {}: reserving stock for items", orderId);

        if (itemsNode == null || !itemsNode.isArray() || itemsNode.size() == 0) {
            log.warn("No items found in order {}", orderId);
            return;
        }

        java.util.List<com.ecommerce.inventory.application.dto.ReserveStockCommand> successfulReservations = new java.util.ArrayList<>();
        boolean allSuccessful = true;
        String failureReason = "";

        for (com.fasterxml.jackson.databind.JsonNode item : itemsNode) {
            String productId = item.get("productId").asText();
            int quantity = item.get("quantity").asInt();
            String reservationId = orderId;

            var command = new com.ecommerce.inventory.application.dto.ReserveStockCommand(productId, quantity,
                    reservationId);
            var result = reserveStock(command);

            if (result.success()) {
                successfulReservations.add(command);
                eventProducer.publishStockReserved(productId, orderId, quantity, result.availableQuantity());
            } else {
                allSuccessful = false;
                failureReason = result.message();
                log.warn("Failed to reserve product {} for order {}: {}", productId, orderId, failureReason);
                eventProducer.publishStockReservationFailed(productId, orderId, quantity, result.availableQuantity());
                break;
            }
        }

        if (allSuccessful) {
            log.info("Successfully reserved all items for order {}", orderId);
            eventProducer.publishAllItemsReserved(orderId);
        } else {
            log.error("Failed to reserve all items for order {}. Rolling back {} successful reservations.",
                    orderId, successfulReservations.size());

            // Rollback successful reservations in this batch
            for (var cmd : successfulReservations) {
                try {
                    releaseStock(new com.ecommerce.inventory.application.dto.ReleaseStockCommand(
                            cmd.productId(), cmd.quantity(), cmd.reservationReference()));
                } catch (Exception e) {
                    log.error("Failed to rollback reservation for product {} in order {}: {}",
                            cmd.productId(), orderId, e.getMessage());
                }
            }
            // The StockReservationFailed for the failing item was already sent in the loop
        }
    }

    /**
     * Handle OrderCancelled event.
     * Releases stock if it was reserved.
     */
    @Transactional
    public void handleOrderCancelled(String orderId, com.fasterxml.jackson.databind.JsonNode itemsNode) {
        log.info("Handling OrderCancelled for order {}: releasing stock for {} items",
                orderId, itemsNode != null ? itemsNode.size() : 0);

        if (itemsNode != null && itemsNode.isArray()) {
            itemsNode.forEach(item -> {
                String productId = item.get("productId").asText();
                int quantity = item.get("quantity").asInt();
                String reservationId = orderId;

                log.debug("Releasing stock: productId={}, quantity={}, orderId={}",
                        productId, quantity, orderId);

                try {
                    releaseStock(new ReleaseStockCommand(productId, quantity, reservationId));
                } catch (Exception e) {
                    log.error("Failed to release stock for product {} in order {}: {}",
                            productId, orderId, e.getMessage());
                }
            });
        }
    }

    private Inventory findByProductIdOrThrow(String productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Inventory", productId));
    }
}
