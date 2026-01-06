package com.ecommerce.inventory.domain.model;

import com.ecommerce.inventory.domain.event.StockDepleted;
import com.ecommerce.inventory.domain.event.StockReserved;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryDomainTest {

    private final String productId = "prod_123";
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = Inventory.create(productId, 100);
    }

    @Test
    @DisplayName("Should successfully reserve stock when available")
    void reserve_ShouldSucceed_WhenEnoughStock() {
        // When
        StockOperationResult result = inventory.reserve(10, "ref_1");

        // Then
        assertThat(result).isInstanceOf(StockOperationResult.Success.class);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(90);
        assertThat(inventory.getReservedQuantity()).isEqualTo(10);

        // Verify event registration
        assertThat(inventory.getDomainEvents()).hasSize(1);
        assertThat(inventory.getDomainEvents().get(0)).isInstanceOf(StockReserved.class);
    }

    @Test
    @DisplayName("Should fail reserve when stock is insufficient")
    void reserve_ShouldFail_WhenInsufficientStock() {
        // When
        StockOperationResult result = inventory.reserve(150, "ref_1");

        // Then
        assertThat(result).isInstanceOf(StockOperationResult.InsufficientStock.class);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(100);
        assertThat(inventory.getReservedQuantity()).isEqualTo(0);
        assertThat(inventory.getDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("Should register StockDepleted event when reaching zero stock")
    void reserve_ShouldRegisterDepletedEvent_WhenStockReachesZero() {
        // When
        inventory.reserve(100, "ref_1");

        // Then
        assertThat(inventory.getAvailableQuantity()).isZero();
        assertThat(inventory.getDomainEvents()).hasSize(2);
        assertThat(inventory.getDomainEvents().get(1)).isInstanceOf(StockDepleted.class);
    }

    @Test
    @DisplayName("Should successfully release reserved stock")
    void release_ShouldSucceed_WhenReservationExists() {
        // Given
        inventory.reserve(20, "ref_1");
        inventory.clearDomainEvents();

        // When
        StockOperationResult result = inventory.release(20, "ref_1");

        // Then
        assertThat(result).isInstanceOf(StockOperationResult.Success.class);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(100);
        assertThat(inventory.getReservedQuantity()).isZero();
        assertThat(inventory.getDomainEvents()).hasSize(1);
    }

    @Test
    @DisplayName("Should fail release when requesting more than reserved")
    void release_ShouldFail_WhenReleasingMoreThanReserved() {
        // Given
        inventory.reserve(10, "ref_1");

        // When
        StockOperationResult result = inventory.release(20, "ref_1");

        // Then
        assertThat(result).isInstanceOf(StockOperationResult.ReservationNotFound.class);
        assertThat(inventory.getReservedQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should successfully confirm reservation")
    void confirm_ShouldSucceed_WhenReservationExists() {
        // Given
        inventory.reserve(30, "ref_1");

        // When
        StockOperationResult result = inventory.confirm(30, "ref_1");

        // Then
        assertThat(result).isInstanceOf(StockOperationResult.Success.class);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(70);
        assertThat(inventory.getReservedQuantity()).isZero();
    }

    @Test
    @DisplayName("Should successfully add stock")
    void addStock_ShouldIncreaseAvailableQuantity() {
        // When
        inventory.addStock(50);

        // Then
        assertThat(inventory.getAvailableQuantity()).isEqualTo(150);
        assertThat(inventory.getTotalQuantity()).isEqualTo(150);
    }

    @Test
    @DisplayName("Should throw exception when adding invalid stock quantity")
    void addStock_ShouldThrow_WhenQuantityIsInvalid() {
        assertThatThrownBy(() -> inventory.addStock(0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> inventory.addStock(-10))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
