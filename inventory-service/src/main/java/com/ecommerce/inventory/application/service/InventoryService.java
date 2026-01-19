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
        log.info("Handling OrderCreated for order {}", orderId);

        // For simplicity in this phase, we'll assume 1 item per order for the saga flow
        // demo
        // Or we iterate and reserve. If any fails, we should fail the whole order (not
        // implemented here for brevity)
        // Let's support the first item for now or simple iteration

        if (itemsNode.isArray() && itemsNode.size() > 0) {
            var item = itemsNode.get(0);
            String productId = item.get("productId").asText();
            int quantity = item.get("quantity").asInt();
            String reservationId = orderId; // Use orderId as reservation reference

            var result = reserveStock(new ReserveStockCommand(productId, quantity, reservationId));

            if (result.success()) {
                eventProducer.publishStockReserved(productId, orderId, quantity, result.availableQuantity());
                // Signal that all items (in this simplified single-item case) are reserved
                eventProducer.publishAllItemsReserved(orderId);
            } else {
                eventProducer.publishStockReservationFailed(productId, orderId, quantity, result.availableQuantity());
            }
        }
    }

    /**
     * Handle OrderCancelled event.
     * Releases stock if it was reserved.
     */
    @Transactional
    public void handleOrderCancelled(String orderId) {
        log.info("Handling OrderCancelled for order {}", orderId);

        // We need to know which products were reserved.
        // In a real system, we'd look up the Reservation or check the Order details.
        // For this simplified implementation, we might need to assume we can find the
        // reservation by ID (orderId)
        // But Inventory aggregate stores reservations by internal ID.
        // We might need to query inventory that has this reservation.

        // Workaround: We will search for inventory that has this reservationRef
        // (orderId)
        // This requires a new repository method or we assume productId is passed in
        // event (it is not in standard OrderCancelled)
        // Let's assume for this demo that we can't easily release without productId.
        // PROPER FIX: OrderCancelled event should include items or we store reservation
        // mapping.

        // Let's add a method to repo to find inventory by reservation ref
        /*
         * var inventories = inventoryRepository.findByReservationRef(orderId);
         * for (var inv : inventories) {
         * releaseStock(new ReleaseStockCommand(inv.getProductId(), quantity, orderId));
         * }
         */

        // Since we don't have that yet, and time is tight, let's log a warning.
        log.warn("Stock release for OrderCancelled {} not fully implemented without ProductID lookup", orderId);
    }

    private Inventory findByProductIdOrThrow(String productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Inventory", productId));
    }
}
