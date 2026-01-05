package com.ecommerce.inventory.domain.model;

import com.ecommerce.common.domain.AggregateRoot;
import com.ecommerce.common.util.IdGenerator;
import com.ecommerce.inventory.domain.event.StockDepleted;
import com.ecommerce.inventory.domain.event.StockReleased;
import com.ecommerce.inventory.domain.event.StockReserved;

import java.time.Instant;
import java.util.Objects;

/**
 * Inventory aggregate root.
 * Manages stock levels for a product with reservation support.
 */
public class Inventory extends AggregateRoot<InventoryId> {

    private final InventoryId id;
    private final String productId;
    private StockQuantity availableQuantity;
    private StockQuantity reservedQuantity;
    private Long version; // For optimistic locking
    private final Instant createdAt;
    private Instant updatedAt;

    private Inventory(InventoryId id, String productId, StockQuantity availableQuantity,
            StockQuantity reservedQuantity, Long version, Instant createdAt) {
        this.id = id;
        this.productId = productId;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     * Create new inventory for a product.
     */
    public static Inventory create(String productId, int initialQuantity) {
        Objects.requireNonNull(productId, "Product ID must not be null");
        if (productId.isBlank()) {
            throw new IllegalArgumentException("Product ID must not be blank");
        }

        var inventoryId = new InventoryId(IdGenerator.generate("inv"));
        return new Inventory(
                inventoryId,
                productId,
                StockQuantity.of(initialQuantity),
                StockQuantity.zero(),
                null,
                Instant.now());
    }

    /**
     * Reconstitute from persistence.
     */
    public static Inventory reconstitute(InventoryId id, String productId,
            int availableQuantity, int reservedQuantity,
            Long version, Instant createdAt, Instant updatedAt) {
        var inventory = new Inventory(
                id,
                productId,
                StockQuantity.of(availableQuantity),
                StockQuantity.of(reservedQuantity),
                version,
                createdAt);
        inventory.updatedAt = updatedAt;
        return inventory;
    }

    /**
     * Reserve stock for an order.
     * This moves quantity from available to reserved.
     */
    public StockOperationResult reserve(int quantity, String reservationReference) {
        Objects.requireNonNull(reservationReference, "Reservation reference required");

        var requestedQuantity = StockQuantity.of(quantity);

        // Check if enough stock available
        if (!availableQuantity.isGreaterThanOrEqual(requestedQuantity)) {
            return new StockOperationResult.InsufficientStock(
                    id.value(),
                    quantity,
                    availableQuantity.value());
        }

        // Move from available to reserved
        availableQuantity = availableQuantity.subtract(requestedQuantity);
        reservedQuantity = reservedQuantity.add(requestedQuantity);
        updatedAt = Instant.now();

        // Register domain event
        registerEvent(new StockReserved(
                IdGenerator.generate(),
                Instant.now(),
                id.value(),
                productId,
                quantity,
                reservationReference));

        // Check if stock depleted
        if (availableQuantity.isZero()) {
            registerEvent(new StockDepleted(
                    IdGenerator.generate(),
                    Instant.now(),
                    id.value(),
                    productId));
        }

        return new StockOperationResult.Success(
                id.value(),
                availableQuantity.value(),
                reservedQuantity.value());
    }

    /**
     * Release reserved stock (e.g., order cancelled).
     * This moves quantity from reserved back to available.
     */
    public StockOperationResult release(int quantity, String reservationReference) {
        Objects.requireNonNull(reservationReference, "Reservation reference required");

        var releaseQuantity = StockQuantity.of(quantity);

        // Check if enough reserved stock
        if (!reservedQuantity.isGreaterThanOrEqual(releaseQuantity)) {
            return new StockOperationResult.ReservationNotFound(
                    id.value(),
                    reservationReference);
        }

        // Move from reserved back to available
        reservedQuantity = reservedQuantity.subtract(releaseQuantity);
        availableQuantity = availableQuantity.add(releaseQuantity);
        updatedAt = Instant.now();

        // Register domain event
        registerEvent(new StockReleased(
                IdGenerator.generate(),
                Instant.now(),
                id.value(),
                productId,
                quantity,
                reservationReference));

        return new StockOperationResult.Success(
                id.value(),
                availableQuantity.value(),
                reservedQuantity.value());
    }

    /**
     * Confirm reservation (order completed).
     * This permanently removes quantity from reserved.
     */
    public StockOperationResult confirm(int quantity, String reservationReference) {
        Objects.requireNonNull(reservationReference, "Reservation reference required");

        var confirmQuantity = StockQuantity.of(quantity);

        // Check if enough reserved stock
        if (!reservedQuantity.isGreaterThanOrEqual(confirmQuantity)) {
            return new StockOperationResult.ReservationNotFound(
                    id.value(),
                    reservationReference);
        }

        // Permanently remove from reserved (stock sold)
        reservedQuantity = reservedQuantity.subtract(confirmQuantity);
        updatedAt = Instant.now();

        return new StockOperationResult.Success(
                id.value(),
                availableQuantity.value(),
                reservedQuantity.value());
    }

    /**
     * Add stock (replenishment).
     */
    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        availableQuantity = availableQuantity.add(StockQuantity.of(quantity));
        updatedAt = Instant.now();
    }

    // Getters
    @Override
    public InventoryId getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public int getAvailableQuantity() {
        return availableQuantity.value();
    }

    public int getReservedQuantity() {
        return reservedQuantity.value();
    }

    public int getTotalQuantity() {
        return availableQuantity.value() + reservedQuantity.value();
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
