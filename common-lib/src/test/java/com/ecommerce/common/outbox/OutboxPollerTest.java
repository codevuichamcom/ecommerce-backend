package com.ecommerce.common.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class OutboxPollerTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private OutboxPoller outboxPoller;
    private final String topicName = "test-topic";

    @BeforeEach
    void setUp() {
        outboxPoller = new OutboxPoller(outboxRepository, kafkaTemplate, topicName, 10);
    }

    @Test
    void pollAndPublish_ShouldPublishMessages_WhenUnpublishedExist() {
        // Given
        OutboxMessage message = new OutboxMessage(
                UUID.randomUUID().toString(),
                "Order",
                "agg_123",
                "OrderCreated",
                "{}",
                Instant.now(),
                false,
                null);

        when(outboxRepository.findUnpublishedForUpdate(10)).thenReturn(List.of(message));
        when(kafkaTemplate.send(eq(topicName), eq(message.aggregateId()), eq(message.payload())))
                .thenReturn(CompletableFuture.completedFuture(null));

        // When
        outboxPoller.pollAndPublish();

        // Then
        verify(kafkaTemplate).send(eq(topicName), eq(message.aggregateId()), eq(message.payload()));
        verify(outboxRepository).markAsPublished(message.id());
    }

    @Test
    void pollAndPublish_ShouldNotMarkAsPublished_WhenKafkaFails() {
        // Given
        OutboxMessage message = new OutboxMessage(
                UUID.randomUUID().toString(),
                "Order",
                "agg_123",
                "OrderCreated",
                "{}",
                Instant.now(),
                false,
                null);

        when(outboxRepository.findUnpublishedForUpdate(10)).thenReturn(List.of(message));
        CompletableFuture<org.springframework.kafka.support.SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka error"));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(failedFuture);

        // When
        outboxPoller.pollAndPublish();

        // Then
        verify(kafkaTemplate).send(eq(topicName), eq(message.aggregateId()), eq(message.payload()));
        verify(outboxRepository, never()).markAsPublished(any());
    }

    @Test
    void pollAndPublish_ShouldDoNothing_WhenNoMessages() {
        // Given
        when(outboxRepository.findUnpublishedForUpdate(10)).thenReturn(Collections.emptyList());

        // When
        outboxPoller.pollAndPublish();

        // Then
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void cleanupOldMessages_ShouldCallRepository() {
        // When
        outboxPoller.cleanupOldMessages();

        // Then
        verify(outboxRepository).deletePublishedOlderThan(7);
    }
}
