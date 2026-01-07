package com.ecommerce.common.outbox;

import com.ecommerce.common.domain.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Utility class for creating outbox messages from domain events.
 * Can be used by any service to store events in the outbox.
 */
public class OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);
    private static final ObjectMapper objectMapper = createObjectMapper();

    private final OutboxRepository outboxRepository;

    public OutboxEventPublisher(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Store a domain event in the outbox.
     * This should be called within the same transaction as the aggregate mutation.
     *
     * @param aggregateType Type of aggregate (e.g., "Order", "Payment")
     * @param aggregateId   ID of the aggregate
     * @param event         The domain event to store
     */
    public void publish(String aggregateType, String aggregateId, DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxMessage message = OutboxMessage.create(
                    UUID.randomUUID().toString(),
                    aggregateType,
                    aggregateId,
                    event.eventType(),
                    payload);

            outboxRepository.save(message);

            log.debug("Stored event in outbox: type={}, aggregateId={}, eventType={}",
                    aggregateType, aggregateId, event.eventType());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event for outbox: {}", event.eventType(), e);
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    /**
     * Get the ObjectMapper used for serialization.
     * Can be used by consumers to deserialize events.
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
