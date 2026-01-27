package com.ecommerce.notification.infrastructure.kafka;

import com.ecommerce.common.kafka.IdempotentEventHandler;
import com.ecommerce.common.kafka.KafkaTopics;
import com.ecommerce.common.kafka.ProcessedEventRepository;
import com.ecommerce.notification.application.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Kafka consumer for notification events.
 * Listens to order and payment events and sends appropriate notifications.
 */
@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);
    private static final ObjectMapper objectMapper = createObjectMapper();

    private final NotificationService notificationService;
    private final IdempotentEventHandler idempotentEventHandler;

    public NotificationEventConsumer(
            NotificationService notificationService,
            ProcessedEventRepository processedEventRepository) {
        this.notificationService = notificationService;
        this.idempotentEventHandler = new IdempotentEventHandler(processedEventRepository);
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Listen for order events and send notifications.
     */
    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = KafkaTopics.NOTIFICATION_SERVICE_GROUP)
    public void handleOrderEvent(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String eventType = extractEventType(jsonNode, "order");
            String eventId = jsonNode.get("eventId").asText();

            log.debug("Received order event: type={}, id={}", eventType, eventId);

            switch (eventType) {
                case "OrderConfirmed" -> handleOrderConfirmed(jsonNode, eventId);
                case "OrderCancelled" -> handleOrderCancelled(jsonNode, eventId);
                default -> log.debug("Ignoring order event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Error processing order event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process order event", e);
        }
    }

    /**
     * Listen for payment events and send notifications.
     */
    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = KafkaTopics.NOTIFICATION_SERVICE_GROUP)
    public void handlePaymentEvent(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String eventType = extractEventType(jsonNode, "payment");
            String eventId = jsonNode.get("eventId").asText();

            log.debug("Received payment event: type={}, id={}", eventType, eventId);

            switch (eventType) {
                case "PaymentCompleted" -> handlePaymentCompleted(jsonNode, eventId);
                case "PaymentFailed" -> handlePaymentFailed(jsonNode, eventId);
                default -> log.debug("Ignoring payment event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Error processing payment event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process payment event", e);
        }
    }

    private void handleOrderConfirmed(JsonNode jsonNode, String eventId) {
        idempotentEventHandler.processIdempotently(eventId, "OrderConfirmed", () -> {
            try {
                String orderId = jsonNode.get("orderId").asText();
                String customerId = jsonNode.get("customerId").asText();
                BigDecimal totalAmount = new BigDecimal(jsonNode.get("totalAmount").asText());
                String currency = jsonNode.get("currency").asText();

                // For now, use a placeholder email (in production, fetch from user service)
                String customerEmail = customerId + "@example.com";

                log.info("Sending order confirmation notification for order: {}", orderId);

                notificationService.sendOrderConfirmation(
                        customerId,
                        customerEmail,
                        orderId,
                        totalAmount + " " + currency);

            } catch (Exception e) {
                log.error("Error processing OrderConfirmed event: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process OrderConfirmed event", e);
            }
        });
    }

    private void handleOrderCancelled(JsonNode jsonNode, String eventId) {
        idempotentEventHandler.processIdempotently(eventId, "OrderCancelled", () -> {
            try {
                String orderId = jsonNode.get("orderId").asText();
                String customerId = jsonNode.get("customerId").asText();
                String reason = jsonNode.has("reason") ? jsonNode.get("reason").asText() : "No reason provided";

                String customerEmail = customerId + "@example.com";

                log.info("Sending order cancellation notification for order: {}", orderId);

                notificationService.sendOrderCancelled(
                        customerId,
                        customerEmail,
                        orderId,
                        reason);

            } catch (Exception e) {
                log.error("Error processing OrderCancelled event: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process OrderCancelled event", e);
            }
        });
    }

    private void handlePaymentCompleted(JsonNode jsonNode, String eventId) {
        idempotentEventHandler.processIdempotently(eventId, "PaymentCompleted", () -> {
            try {
                String orderId = jsonNode.get("orderId").asText();
                String customerId = jsonNode.get("customerId").asText();
                BigDecimal amount = new BigDecimal(jsonNode.get("amount").asText());
                String currency = jsonNode.get("currency").asText();
                String transactionId = jsonNode.get("transactionId").asText();

                String customerEmail = customerId + "@example.com";

                log.info("Sending payment received notification for order: {}", orderId);

                notificationService.sendPaymentReceived(
                        customerId,
                        customerEmail,
                        orderId,
                        amount + " " + currency,
                        transactionId);

            } catch (Exception e) {
                log.error("Error processing PaymentCompleted event: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process PaymentCompleted event", e);
            }
        });
    }

    private void handlePaymentFailed(JsonNode jsonNode, String eventId) {
        idempotentEventHandler.processIdempotently(eventId, "PaymentFailed", () -> {
            try {
                String orderId = jsonNode.get("orderId").asText();
                String customerId = jsonNode.get("customerId").asText();
                String reason = jsonNode.has("reason") ? jsonNode.get("reason").asText() : "Unknown error";

                String customerEmail = customerId + "@example.com";

                log.info("Sending payment failed notification for order: {}", orderId);

                notificationService.sendPaymentFailed(
                        customerId,
                        customerEmail,
                        orderId,
                        reason);

            } catch (Exception e) {
                log.error("Error processing PaymentFailed event: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process PaymentFailed event", e);
            }
        });
    }

    private String extractEventType(JsonNode jsonNode, String defaultPrefix) {
        if (jsonNode.has("eventType")) {
            return jsonNode.get("eventType").asText();
        }
        // Infer from structure
        if (jsonNode.has("transactionId") && jsonNode.has("amount")) {
            return "PaymentCompleted";
        }
        if (jsonNode.has("errorCode") && jsonNode.has("reason")) {
            return "PaymentFailed";
        }
        if (jsonNode.has("requiresRefund")) {
            return "OrderCancelled";
        }
        if (jsonNode.has("totalAmount") && !jsonNode.has("items")) {
            return "OrderConfirmed";
        }
        return "Unknown";
    }
}
