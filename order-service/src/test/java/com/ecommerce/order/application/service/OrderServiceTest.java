package com.ecommerce.order.application.service;

import com.ecommerce.common.exception.ConflictException;
import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.order.application.dto.CreateOrderCommand;
import com.ecommerce.order.application.dto.OrderItemRequest;
import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.port.out.InventoryServicePort;
import com.ecommerce.order.application.port.out.InventoryServicePort.ReservationResult;
import com.ecommerce.order.application.port.out.ProductServicePort;
import com.ecommerce.order.application.port.out.ProductServicePort.ProductDetails;
import com.ecommerce.order.domain.model.*;
import com.ecommerce.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductServicePort productService;

    @Mock
    private InventoryServicePort inventoryService;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderCommand createCommand;
    private ProductDetails productDetails;
    private String customerId = "cust_123";
    private String productId = "prod_123";
    private String idempotencyKey = "key_123";

    @BeforeEach
    void setUp() {
        createCommand = new CreateOrderCommand(
                customerId,
                List.of(new OrderItemRequest(productId, 2)));

        productDetails = new ProductDetails(
                productId,
                "Test Product",
                new BigDecimal("50.00"),
                "USD",
                true);
    }

    @Test
    void createOrder_ShouldReturnExistingOrder_WhenIdempotencyKeyMatches() {
        // Given
        Order existingOrder = createTestOrder();
        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingOrder));

        // When
        OrderResponse response = orderService.createOrder(createCommand, idempotencyKey);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(existingOrder.getId().value());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_ShouldCreateAndConfirmOrder_WhenSuccessful() {
        // Given
        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(productService.getProduct(productId)).thenReturn(productDetails);

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        when(inventoryService.reserveStock(anyString(), anyInt(), anyString()))
                .thenReturn(new ReservationResult.Success(10, 2));

        // When
        OrderResponse response = orderService.createOrder(createCommand, idempotencyKey);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("CONFIRMED");
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }

    @Test
    void createOrder_ShouldRollbackAndThrow_WhenInsufficientStock() {
        // Given
        String productId2 = "prod_456";
        createCommand = new CreateOrderCommand(
                customerId,
                List.of(
                        new OrderItemRequest(productId, 2),
                        new OrderItemRequest(productId2, 1)));

        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(productService.getProduct(productId)).thenReturn(productDetails);
        when(productService.getProduct(productId2))
                .thenReturn(new ProductDetails(productId2, "Product 2", BigDecimal.TEN, "USD", true));

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // First succeeds, second fails
        when(inventoryService.reserveStock(eq(productId), eq(2), anyString()))
                .thenReturn(new ReservationResult.Success(10, 2));
        when(inventoryService.reserveStock(eq(productId2), eq(1), anyString()))
                .thenReturn(new ReservationResult.InsufficientStock(1, 0));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(createCommand, idempotencyKey))
                .isInstanceOf(ConflictException.class);

        // Should rollback the first one
        verify(inventoryService).releaseStock(eq(productId), eq(2), anyString());
    }

    @Test
    void getOrder_ShouldReturnOrder_WhenExists() {
        // Given
        Order order = createTestOrder();
        when(orderRepository.findById(any(OrderId.class))).thenReturn(Optional.of(order));

        // When
        OrderResponse response = orderService.getOrder("order_123");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(order.getId().value());
    }

    @Test
    void getOrder_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(orderRepository.findById(any(OrderId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrder("order_123"))
                .isInstanceOf(NotFoundException.class);
    }

    private Order createTestOrder() {
        return Order.create(
                new CustomerId(customerId),
                List.of(OrderItem.create(
                        productId,
                        "Product",
                        1,
                        new Money(BigDecimal.TEN, "USD"))),
                idempotencyKey);
    }
}
