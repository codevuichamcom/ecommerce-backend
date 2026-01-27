package com.ecommerce.inventory.application.service;

import com.ecommerce.common.exception.ConflictException;
import com.ecommerce.inventory.application.dto.*;
import com.ecommerce.inventory.domain.model.Inventory;
import com.ecommerce.inventory.domain.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private com.ecommerce.inventory.infrastructure.kafka.InventoryEventProducer eventProducer;

    @Mock
    private io.micrometer.core.instrument.MeterRegistry meterRegistry;

    @Mock
    private io.micrometer.core.instrument.Counter counter;

    @InjectMocks
    private InventoryService inventoryService;

    private String productId = "prod_123";
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
        inventory = Inventory.create(productId, 100);
    }

    @Test
    void createInventory_ShouldSaveAndReturn_WhenNotExists() {
        // Given
        CreateInventoryCommand command = new CreateInventoryCommand(productId, 100);
        when(inventoryRepository.existsByProductId(productId)).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // When
        InventoryResponse response = inventoryService.createInventory(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.productId()).isEqualTo(productId);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void createInventory_ShouldThrowConflictException_WhenExists() {
        // Given
        CreateInventoryCommand command = new CreateInventoryCommand(productId, 100);
        when(inventoryRepository.existsByProductId(productId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> inventoryService.createInventory(command))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void reserveStock_ShouldSucceed_WhenEnoughStock() {
        // Given
        ReserveStockCommand command = new ReserveStockCommand(productId, 10, "order_1");
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // When
        StockOperationResponse response = inventoryService.reserveStock(command);

        // Then
        assertThat(response.success()).isTrue();
        assertThat(response.availableQuantity()).isEqualTo(90);
        assertThat(response.reservedQuantity()).isEqualTo(10);
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void reserveStock_ShouldFail_WhenInsufficientStock() {
        // Given
        ReserveStockCommand command = new ReserveStockCommand(productId, 150, "order_1");
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        // When
        StockOperationResponse response = inventoryService.reserveStock(command);

        // Then
        assertThat(response.success()).isFalse();
        assertThat(response.message()).contains("Insufficient stock");
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void releaseStock_ShouldSucceed_WhenReservationExists() {
        // Given
        inventory.reserve(10, "order_1");
        ReleaseStockCommand command = new ReleaseStockCommand(productId, 10, "order_1");
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // When
        StockOperationResponse response = inventoryService.releaseStock(command);

        // Then
        assertThat(response.success()).isTrue();
        assertThat(response.availableQuantity()).isEqualTo(100);
        assertThat(response.reservedQuantity()).isEqualTo(0);
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void addStock_ShouldIncreaseQuantity() {
        // Given
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // When
        InventoryResponse response = inventoryService.addStock(productId, 50);

        // Then
        assertThat(response.availableQuantity()).isEqualTo(150);
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void getInventoryByProductId_ShouldReturn_WhenExists() {
        // Given
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        // When
        InventoryResponse response = inventoryService.getInventoryByProductId(productId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.productId()).isEqualTo(productId);
    }
}
