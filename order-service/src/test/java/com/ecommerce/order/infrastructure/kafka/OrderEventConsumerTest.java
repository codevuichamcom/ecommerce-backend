package com.ecommerce.order.infrastructure.kafka;

import com.ecommerce.common.events.InventoryEvents;
import com.ecommerce.order.infrastructure.persistence.repository.ProcessedEventJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class OrderEventConsumerTest {

    @Mock
    private com.ecommerce.order.application.service.OrderService orderService;

    @Mock
    private ProcessedEventJpaRepository processedEventRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private OrderEventConsumer orderEventConsumer;

    @BeforeEach
    void setUp() {
        orderEventConsumer = new OrderEventConsumer(
                orderService,
                processedEventRepository,
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

}
