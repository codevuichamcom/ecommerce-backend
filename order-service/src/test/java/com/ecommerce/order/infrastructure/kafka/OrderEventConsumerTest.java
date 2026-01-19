package com.ecommerce.order.infrastructure.kafka;

import com.ecommerce.common.events.InventoryEvents;
import com.ecommerce.order.domain.model.CustomerId;
import com.ecommerce.order.domain.model.Money;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderId;
import com.ecommerce.order.domain.model.OrderItem;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.order.domain.saga.OrderSaga;
import com.ecommerce.order.domain.saga.OrderSagaRepository;
import com.ecommerce.order.infrastructure.persistence.repository.ProcessedEventJpaRepository;
import com.ecommerce.common.outbox.OutboxEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class OrderEventConsumerTest {

    @Mock
    private com.ecommerce.order.application.service.OrderService orderService;

    @Mock
    private ProcessedEventJpaRepository processedEventRepository;

    @Mock
    private io.micrometer.core.instrument.MeterRegistry meterRegistry;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private OrderEventConsumer orderEventConsumer;

    @BeforeEach
    void setUp() {
        orderEventConsumer = new OrderEventConsumer(
                orderService,
                processedEventRepository,
                meterRegistry,
                objectMapper);
    }

    @Test
    void handleInventoryEvents_ShouldProcessAllItemsReserved() throws Exception {
        // Given
        String orderIdStr = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();
        InventoryEvents.AllItemsReserved event = new InventoryEvents.AllItemsReserved(
                eventId, Instant.now(), null, orderIdStr);
        String message = objectMapper.writeValueAsString(event);

        when(processedEventRepository.existsById(any(UUID.class))).thenReturn(false);

        // When
        orderEventConsumer.handleInventoryEvents(message);

        // Then
        verify(orderService).onInventoryReserved(orderIdStr);
        verify(processedEventRepository).save(any());
    }

    @Test
    void handleInventoryEvents_ShouldSkip_WhenAlreadyProcessed() throws Exception {
        // Given
        String eventId = UUID.randomUUID().toString();
        String message = "{\"eventId\":\"" + eventId + "\"}";
        when(processedEventRepository.existsById(any(UUID.class))).thenReturn(true);

        // When
        orderEventConsumer.handleInventoryEvents(message);

        // Then
        verify(orderService, never()).onInventoryReserved(any());
    }

    @Test
    void handlePaymentEvents_ShouldProcessPaymentCompleted() throws Exception {
        // Given
        String orderIdStr = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();
        com.ecommerce.common.events.PaymentEvents.PaymentCompleted event = new com.ecommerce.common.events.PaymentEvents.PaymentCompleted(
                eventId, Instant.now(), UUID.randomUUID().toString(), orderIdStr,
                "cust_123", BigDecimal.TEN, "USD", "TXN-123");
        String message = objectMapper.writeValueAsString(event);

        when(processedEventRepository.existsById(any(UUID.class))).thenReturn(false);

        // When
        orderEventConsumer.handlePaymentEvents(message);

        // Then
        verify(orderService).onPaymentCompleted(orderIdStr);
        verify(processedEventRepository).save(any());
    }

    private Order createTestOrder(String id) {
        return Order.create(
                new CustomerId("cust_123"),
                List.of(OrderItem.create("prod_1", "Product", 1, new Money(BigDecimal.TEN, "USD"))),
                "key_123");
    }
}
