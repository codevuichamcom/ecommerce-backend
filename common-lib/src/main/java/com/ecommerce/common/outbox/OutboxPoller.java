package com.ecommerce.common.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Scheduled job that polls the outbox table and publishes events to Kafka.
 * This ensures reliable event delivery with at-least-once semantics.
 * 
 * Each service should create a bean of this class with appropriate
 * configuration.
 */
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);
    private static final int DEFAULT_BATCH_SIZE = 100;

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topicName;
    private final int batchSize;
    private final int daysToKeep;

    public OutboxPoller(
            OutboxRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            String topicName) {
        this(outboxRepository, kafkaTemplate, topicName, DEFAULT_BATCH_SIZE, 7);
    }

    public OutboxPoller(
            OutboxRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            String topicName,
            int batchSize) {
        this(outboxRepository, kafkaTemplate, topicName, batchSize, 7);
    }

    public OutboxPoller(
            OutboxRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            String topicName,
            int batchSize,
            int daysToKeep) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
        this.batchSize = batchSize;
        this.daysToKeep = daysToKeep;
    }

    /**
     * Polls the outbox and publishes unpublished messages to Kafka.
     * Runs every 1 second by default.
     */
    @Scheduled(fixedDelayString = "${outbox.poll.interval-ms:1000}")
    @Transactional
    public void pollAndPublish() {
        List<OutboxMessage> messages = outboxRepository.findUnpublishedForUpdate(batchSize);

        if (messages.isEmpty()) {
            return;
        }

        log.debug("Found {} unpublished outbox messages", messages.size());

        for (OutboxMessage message : messages) {
            try {
                publishToKafka(message);
                outboxRepository.markAsPublished(message.id());
                log.debug("Published outbox message: id={}, type={}", message.id(), message.eventType());
            } catch (Exception e) {
                log.error("Failed to publish outbox message: id={}, error={}", message.id(), e.getMessage());
                // Don't throw - continue with other messages
            }
        }
    }

    @SuppressWarnings("null")
    private void publishToKafka(OutboxMessage message) {
        // Use aggregateId as the key for consistent partitioning
        CompletableFuture<?> future = kafkaTemplate.send(
                topicName,
                message.aggregateId(),
                message.payload());

        // Wait for send to complete (or fail)
        try {
            future.get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to Kafka", e);
        }
    }

    /**
     * Cleanup old published messages.
     * Runs daily at 3 AM.
     */
    @Scheduled(cron = "${outbox.cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void cleanupOldMessages() {
        log.info("Cleaning up published outbox messages older than {} days", daysToKeep);
        outboxRepository.deletePublishedOlderThan(daysToKeep);
    }
}
