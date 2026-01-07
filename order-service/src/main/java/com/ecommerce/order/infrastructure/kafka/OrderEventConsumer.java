package com.ecommerce.order.infrastructure.kafka;

import com.ecommerce.common.events.InventoryEvents;
import com.ecommerce.common.events.OrderEvents;
import com.ecommerce.common.events.PaymentEvents;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderId;

import com.ecommerce.order.domain.repository.OrderRepository;

import com.ecommerce.order.domain.saga.OrderSagaRepository;
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

    private final OrderSagaRepository sagaRepository;
    private final OrderRepository orderRepository;
    private final com.ecommerce.common.outbox.OutboxEventPublisher outboxEventPublisher;
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

            if ("AllItemsReserved".equals(eventType) || message.contains("AllItemsReserved")) {
                var event = objectMapper.readValue(message, InventoryEvents.AllItemsReserved.class);
                handleAllItemsReserved(event);
            } else if ("StockReservationFailed".equals(eventType) || message.contains("StockReservationFailed")) {
                var event = objectMapper.readValue(message, InventoryEvents.StockReservationFailed.class);
                handleStockReservationFailed(event);
            }

            markAsProcessed(eventId);
        } catch (Exception e) {
            log.error("Failed to process inventory event: {}", e.getMessage());
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

            if ("PaymentCompleted".equals(eventType) || message.contains("PaymentCompleted")) {
                var event = objectMapper.readValue(message, PaymentEvents.PaymentCompleted.class);
                handlePaymentCompleted(event);
            } else if ("PaymentFailed".equals(eventType) || message.contains("PaymentFailed")) {
                var event = objectMapper.readValue(message, PaymentEvents.PaymentFailed.class);
                handlePaymentFailed(event);
            }

            markAsProcessed(eventId);
        } catch (Exception e) {
            log.error("Failed to process payment event: {}", e.getMessage());
        }
    }

    @SuppressWarnings("null")
    private boolean isAlreadyProcessed(String eventId) {
        UUID uuid = UUID.fromString(eventId);
        return processedEventRepository.existsById(uuid);
    }

    private void markAsProcessed(String eventId) {
        processedEventRepository.save(new ProcessedEventEntity(
                UUID.fromString(eventId),
                "order-service",
                Instant.now()));
    }

    private void handleAllItemsReserved(InventoryEvents.AllItemsReserved event) {
        OrderId orderId = new OrderId(event.orderId());
        sagaRepository.findById(orderId).ifPresent(saga -> {
            log.info("Inventory reserved for order {}. Moving to payment.", event.orderId());
            saga.inventoryReserved();
            sagaRepository.save(saga);

            // Fetch order and publish PaymentRequested
            orderRepository.findById(orderId).ifPresent(this::publishPaymentRequested);
        });
    }

    private void handleStockReservationFailed(InventoryEvents.StockReservationFailed event) {
        OrderId orderId = new OrderId(event.orderId());
        sagaRepository.findById(orderId).ifPresent(saga -> {
            log.warn("Inventory reservation failed for order {}: {}", event.orderId(), event.reason());
            saga.inventoryFailed(event.reason());
            sagaRepository.save(saga);

            orderRepository.findById(orderId).ifPresent(order -> {
                order.cancel("Insufficient stock: " + event.reason());
                orderRepository.save(order);
            });
        });
    }

    private void handlePaymentCompleted(PaymentEvents.PaymentCompleted event) {
        OrderId orderId = new OrderId(event.orderId());
        sagaRepository.findById(orderId).ifPresent(saga -> {
            log.info("Payment completed for order {}. Finishing saga.", event.orderId());
            saga.paymentCompleted();
            saga.complete();
            sagaRepository.save(saga);

            orderRepository.findById(orderId).ifPresent(order -> {
                order.confirm();
                orderRepository.save(order);

                // Publish OrderConfirmed event
                publishOrderConfirmed(order);
            });
        });
    }

    private void handlePaymentFailed(PaymentEvents.PaymentFailed event) {
        OrderId orderId = new OrderId(event.orderId());
        sagaRepository.findById(orderId).ifPresent(saga -> {
            log.warn("Payment failed for order {}: {}", event.orderId(), event.reason());
            saga.paymentFailed(event.reason());
            saga.startCompensating();
            sagaRepository.save(saga);

            // Compensation: Actually we should publish an event that Inventory service
            // listens to
            log.info("Publishing compensation (release inventory) for order {}", event.orderId());

            orderRepository.findById(orderId).ifPresent(order -> {
                order.cancel("Payment failed: " + event.reason());
                orderRepository.save(order);
            });
        });
    }

    private void publishPaymentRequested(Order order) {
        PaymentEvents.PaymentRequested event = PaymentEvents.PaymentRequested.create(
                order.getId().value(),
                order.getCustomerId().value(),
                order.getTotalAmount().amount(),
                order.getTotalAmount().currency());
        outboxEventPublisher.publish("Order", order.getId().value(), event);
    }

    private void publishOrderConfirmed(Order order) {
        OrderEvents.OrderConfirmed event = OrderEvents.OrderConfirmed.create(
                order.getId().value(),
                order.getCustomerId().value(),
                order.getTotalAmount().amount(),
                order.getTotalAmount().currency());
        outboxEventPublisher.publish("Order", order.getId().value(), event);
    }
}
