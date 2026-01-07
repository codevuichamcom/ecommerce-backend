package com.ecommerce.order.application.service;

import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.order.application.dto.CreateOrderCommand;
import com.ecommerce.order.application.dto.OrderItemRequest;
import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.port.out.ProductServicePort;
import com.ecommerce.order.application.port.out.ProductServicePort.ProductDetails;
import com.ecommerce.order.domain.model.*;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.order.domain.saga.OrderSagaRepository;
import com.ecommerce.common.outbox.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

        @Mock
        private OrderRepository orderRepository;

        @Mock
        private ProductServicePort productService;

        @Mock
        private OrderSagaRepository sagaRepository;

        @Mock
        private OutboxRepository outboxRepository;

        @Mock
        private ObjectMapper objectMapper;

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
        void createOrder_ShouldCreateAndConfirmOrder_WhenSuccessful() throws Exception {
                // Given
                when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
                when(productService.getProduct(productId)).thenReturn(productDetails);
                when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

                // When
                OrderResponse response = orderService.createOrder(createCommand, idempotencyKey);

                // Then
                assertThat(response).isNotNull();
                // Note: In the new implementation, status remains PENDING until saga processes
                // it
                assertThat(response.status()).isEqualTo("PENDING");
                verify(orderRepository).save(any(Order.class));
                verify(sagaRepository).save(any());
                verify(outboxRepository).save(any());
        }

        // Removed createOrder_ShouldRollbackAndThrow_WhenInsufficientStock as it is now
        // handled asynchronously by the Saga

        @Test
        void createOrder_ShouldThrowValidationException_WhenProductInactive() {
                // Given
                productDetails = new ProductDetails(productId, "Inactive", BigDecimal.TEN, "USD", false);
                when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
                when(productService.getProduct(productId)).thenReturn(productDetails);

                // When & Then
                assertThatThrownBy(() -> orderService.createOrder(createCommand, idempotencyKey))
                                .isInstanceOf(com.ecommerce.common.exception.ValidationException.class);
        }

        // Removed createOrder_ShouldRollback_WhenInventoryServiceUnavailable as it is
        // now handled asynchronously by the Saga

        @Test
        void cancelOrder_ShouldPublishCancelledEvent() throws Exception {
                // Given
                Order confirmedOrder = createTestOrder();
                confirmedOrder.confirm();
                String orderId = "order_123";

                when(orderRepository.findById(any(OrderId.class))).thenReturn(Optional.of(confirmedOrder));
                when(orderRepository.save(any(Order.class))).thenReturn(confirmedOrder);

                // When
                orderService.cancelOrder(orderId, "Customer request");

                // Then
                assertThat(confirmedOrder.getStatus()).isInstanceOf(OrderStatus.Cancelled.class);
                verify(orderRepository).save(confirmedOrder);
                verify(outboxRepository).save(any());
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
