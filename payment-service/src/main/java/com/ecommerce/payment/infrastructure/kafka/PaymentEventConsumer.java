package com.ecommerce.payment.infrastructure.kafka;

import com.ecommerce.common.kafka.IdempotentEventHandler;
import com.ecommerce.common.kafka.KafkaTopics;
import com.ecommerce.common.kafka.ProcessedEventRepository;

import com.ecommerce.payment.application.dto.ProcessPaymentCommand;
import com.ecommerce.payment.application.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Kafka consumer for order events.
 * Listens for OrderCreated events and triggers payment processing.
 */
@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);
    private static final ObjectMapper objectMapper = createObjectMapper();

    private final PaymentService paymentService;
    private final IdempotentEventHandler idempotentEventHandler;

    public PaymentEventConsumer(
            PaymentService paymentService,
            ProcessedEventRepository processedEventRepository) {
        this.paymentService = paymentService;
        this.idempotentEventHandler = new IdempotentEventHandler(processedEventRepository);
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Listen for order events and process payments.
     */
    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = KafkaTopics.PAYMENT_SERVICE_GROUP)
    public void handleOrderEvent(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String eventType = extractEventType(jsonNode);
            String eventId = jsonNode.get("eventId").asText();

            log.debug("Received order event: type={}, id={}", eventType, eventId);

            switch (eventType) {
                case "OrderCreated" -> handleOrderCreated(jsonNode, eventId);
                case "OrderCancelled" -> handleOrderCancelled(jsonNode, eventId);
                default -> log.debug("Ignoring event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Error processing order event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process order event", e);
        }
    }

    private void handleOrderCreated(JsonNode jsonNode, String eventId) {
        idempotentEventHandler.processIdempotently(eventId, "OrderCreated", () -> {
            try {
                String orderId = jsonNode.get("orderId").asText();
                String customerId = jsonNode.get("customerId").asText();
                BigDecimal totalAmount = new BigDecimal(jsonNode.get("totalAmount").asText());
                String currency = jsonNode.get("currency").asText();

                log.info("Processing payment for order: {}", orderId);

                ProcessPaymentCommand command = new ProcessPaymentCommand(
                        orderId,
                        customerId,
                        totalAmount,
                        currency);

                paymentService.processPayment(command);

            } catch (Exception e) {
                log.error("Error processing OrderCreated event: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process OrderCreated event", e);
            }
        });
    }

    private void handleOrderCancelled(JsonNode jsonNode, String eventId) {
        idempotentEventHandler.processIdempotently(eventId, "OrderCancelled", () -> {
            try {
                String orderId = jsonNode.get("orderId").asText();
                boolean requiresRefund = jsonNode.has("requiresRefund") && jsonNode.get("requiresRefund").asBoolean();

                if (requiresRefund) {
                    log.info("Order cancelled, initiating refund for order: {}", orderId);
                    // Get existing payment and refund
                    try {
                        var payment = paymentService.getPaymentByOrderId(orderId);
                        if ("COMPLETED".equals(payment.status())) {
                            paymentService.refundPayment(payment.id(), "Order cancelled");
                        }
                    } catch (Exception e) {
                        log.warn("Could not find payment to refund for order {}: {}", orderId, e.getMessage());
                    }
                }

            } catch (Exception e) {
                log.error("Error processing OrderCancelled event: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process OrderCancelled event", e);
            }
        });
    }

    private String extractEventType(JsonNode jsonNode) {
        // Try to extract event type from the JSON
        // The event type might be in a field or inferred from the structure
        if (jsonNode.has("eventType")) {
            return jsonNode.get("eventType").asText();
        }
        // Default: check for known fields
        if (jsonNode.has("items") && jsonNode.has("totalAmount")) {
            return "OrderCreated";
        }
        if (jsonNode.has("reason") && jsonNode.has("requiresRefund")) {
            return "OrderCancelled";
        }
        return "Unknown";
    }
}
