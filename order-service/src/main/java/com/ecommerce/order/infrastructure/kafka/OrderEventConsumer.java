package com.ecommerce.order.infrastructure.kafka;

import com.ecommerce.common.events.InventoryEvents;
import com.ecommerce.common.events.PaymentEvents;
import com.ecommerce.order.application.service.OrderService;
import com.ecommerce.order.infrastructure.persistence.entity.ProcessedEventEntity;
import com.ecommerce.order.infrastructure.persistence.repository.ProcessedEventJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderService orderService; // ARCH-001: Delegate to application service
    private final ProcessedEventJpaRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topics.inventory-events:inventory-events}", groupId = "order-service")
    @Transactional
    public void handleInventoryEvents(String message) {
        log.debug("Received inventory event: {}", message);
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventId = node.get("eventId").asText();
            String eventType = node.has("eventType") ? node.get("eventType").asText() : "";

            if (isAlreadyProcessed(eventId)) {
                log.info("Inventory event already processed: {}", eventId);
                return;
            }

            if ("AllItemsReserved".equals(eventType)) {
                var event = objectMapper.readValue(message, InventoryEvents.AllItemsReserved.class);
                handleAllItemsReserved(event);
            } else if ("StockReservationFailed".equals(eventType)) {
                var event = objectMapper.readValue(message, InventoryEvents.StockReservationFailed.class);
                handleStockReservationFailed(event);
            } else {
                log.warn("Unknown inventory event type: {}", eventType);
            }

            markAsProcessed(eventId);
        } catch (Exception e) {
            // CQ-001: Include full stack trace and re-throw for Kafka retry/DLQ
            log.error("Failed to process inventory event: {}", message, e);
            throw new RuntimeException("Failed to process inventory event", e);
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-events:payment-events}", groupId = "order-service")
    @Transactional
    public void handlePaymentEvents(String message) {
        log.debug("Received payment event: {}", message);
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventId = node.get("eventId").asText();
            String eventType = node.has("eventType") ? node.get("eventType").asText() : "";

            if (isAlreadyProcessed(eventId)) {
                log.info("Payment event already processed: {}", eventId);
                return;
            }

            if ("PaymentCompleted".equals(eventType)) {
                var event = objectMapper.readValue(message, PaymentEvents.PaymentCompleted.class);
                handlePaymentCompleted(event);
            } else if ("PaymentFailed".equals(eventType)) {
                var event = objectMapper.readValue(message, PaymentEvents.PaymentFailed.class);
                handlePaymentFailed(event);
            } else {
                log.warn("Unknown payment event type: {}", eventType);
            }

            markAsProcessed(eventId);
        } catch (Exception e) {
            // CQ-001: Include full stack trace and re-throw for Kafka retry/DLQ
            log.error("Failed to process payment event: {}", message, e);
            throw new RuntimeException("Failed to process payment event", e);
        }
    }

    // CQ-005: Remove @SuppressWarnings and add proper null check
    private boolean isAlreadyProcessed(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID cannot be null or blank");
        }
        UUID uuid = UUID.fromString(eventId);
        if (uuid == null) {
            return false;
        }
        return processedEventRepository.existsById(uuid);
    }

    private void markAsProcessed(String eventId) {
        processedEventRepository.save(new ProcessedEventEntity(
                UUID.fromString(eventId),
                "order-service",
                Instant.now()));
    }

    // ARCH-001: Delegate to application service instead of manipulating domain
    // directly
    private void handleAllItemsReserved(InventoryEvents.AllItemsReserved event) {
        orderService.onInventoryReserved(event.orderId());
    }

    private void handleStockReservationFailed(InventoryEvents.StockReservationFailed event) {
        orderService.onInventoryFailed(event.orderId(), event.reason());
    }

    private void handlePaymentCompleted(PaymentEvents.PaymentCompleted event) {
        orderService.onPaymentCompleted(event.orderId());
    }

    private void handlePaymentFailed(PaymentEvents.PaymentFailed event) {
        orderService.onPaymentFailed(event.orderId(), event.reason());
    }
}
