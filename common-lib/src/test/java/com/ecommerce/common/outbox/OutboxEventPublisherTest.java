package com.ecommerce.common.outbox;

import com.ecommerce.common.domain.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherTest {

    @Mock
    private OutboxRepository outboxRepository;

    private OutboxEventPublisher outboxEventPublisher;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        outboxEventPublisher = new OutboxEventPublisher(outboxRepository);
        objectMapper = OutboxEventPublisher.getObjectMapper();
    }

    @Test
    void publish_ShouldSaveMessageToOutbox() throws Exception {
        // Given
        String aggregateType = "Order";
        String aggregateId = UUID.randomUUID().toString();
        TestEvent event = new TestEvent(UUID.randomUUID().toString(), Instant.now());

        // When
        outboxEventPublisher.publish(aggregateType, aggregateId, event);

        // Then
        ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxRepository).save(captor.capture());

        OutboxMessage savedMessage = captor.getValue();
        assertThat(savedMessage.aggregateType()).isEqualTo(aggregateType);
        assertThat(savedMessage.aggregateId()).isEqualTo(aggregateId);
        assertThat(savedMessage.eventType()).isEqualTo("TestEvent");
        assertThat(savedMessage.published()).isFalse();

        // Verify payload matches serialized event
        String expectedPayload = objectMapper.writeValueAsString(event);
        assertThat(savedMessage.payload()).isEqualTo(expectedPayload);
    }

    record TestEvent(String eventId, Instant occurredAt) implements DomainEvent {
        @Override
        public String eventType() {
            return "TestEvent";
        }
    }
}
