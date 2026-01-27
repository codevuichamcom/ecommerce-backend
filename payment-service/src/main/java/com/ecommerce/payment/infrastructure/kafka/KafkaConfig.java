package com.ecommerce.payment.infrastructure.kafka;

import com.ecommerce.common.kafka.KafkaTopics;
import com.ecommerce.common.outbox.OutboxEventPublisher;
import com.ecommerce.common.outbox.OutboxPoller;
import com.ecommerce.common.outbox.OutboxRepository;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Kafka configuration for payment service.
 */
@Configuration
public class KafkaConfig {

    /**
     * Create the payment-events topic if it doesn't exist.
     */
    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Outbox event publisher for storing events in the outbox.
     */
    @Bean
    public OutboxEventPublisher outboxEventPublisher(OutboxRepository outboxRepository,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return new OutboxEventPublisher(outboxRepository, objectMapper);
    }

    /**
     * Outbox poller for publishing events to Kafka.
     */
    @Bean
    public OutboxPoller outboxPoller(
            OutboxRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate) {
        return new OutboxPoller(outboxRepository, kafkaTemplate, KafkaTopics.PAYMENT_EVENTS);
    }
}
