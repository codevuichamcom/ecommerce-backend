package com.ecommerce.inventory.infrastructure.kafka;

import com.ecommerce.common.kafka.IdempotentEventHandler;
import com.ecommerce.common.kafka.KafkaTopics;
import com.ecommerce.common.kafka.ProcessedEventRepository;
import com.ecommerce.inventory.application.service.InventoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);
    private static final ObjectMapper objectMapper = createObjectMapper();

    private final InventoryService inventoryService;
    private final IdempotentEventHandler idempotentEventHandler;

    public InventoryEventConsumer(
            InventoryService inventoryService,
            ProcessedEventRepository processedEventRepository) {
        this.inventoryService = inventoryService;
        this.idempotentEventHandler = new IdempotentEventHandler(processedEventRepository);
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "inventory-service-group")
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
                JsonNode itemsNode = jsonNode.get("items");

                log.info("Reserving stock for order: {}", orderId);

                // Construct command - wait for service refactor to define the exact method
                // We will perform the reservation logic here
                // For now, let's parse the items
                // itemsNode.forEach(item -> ... );

                inventoryService.handleOrderCreated(orderId, itemsNode);

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
                JsonNode itemsNode = jsonNode.get("items");
                log.info("Releasing stock for order: {}", orderId);

                inventoryService.handleOrderCancelled(orderId, itemsNode);

            } catch (Exception e) {
                log.error("Error processing OrderCancelled event: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process OrderCancelled event", e);
            }
        });
    }

    private String extractEventType(JsonNode jsonNode) {
        if (jsonNode.has("eventType")) {
            return jsonNode.get("eventType").asText();
        }
        if (jsonNode.has("items") && jsonNode.has("totalAmount")) {
            return "OrderCreated";
        }
        if (jsonNode.has("reason") && jsonNode.has("requiresRefund")) {
            return "OrderCancelled";
        }
        return "Unknown";
    }
}
