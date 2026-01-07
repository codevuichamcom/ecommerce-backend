package com.ecommerce.payment.infrastructure.persistence.adapter;

import com.ecommerce.common.outbox.OutboxMessage;
import com.ecommerce.common.outbox.OutboxRepository;
import com.ecommerce.payment.infrastructure.persistence.entity.OutboxEventEntity;
import com.ecommerce.payment.infrastructure.persistence.repository.OutboxJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Adapter implementing OutboxRepository using JPA.
 */
@Component
public class OutboxPersistenceAdapter implements OutboxRepository {

    private final OutboxJpaRepository jpaRepository;

    public OutboxPersistenceAdapter(OutboxJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OutboxMessage save(OutboxMessage message) {
        OutboxEventEntity entity = new OutboxEventEntity(
                message.id(),
                message.aggregateType(),
                message.aggregateId(),
                message.eventType(),
                message.payload(),
                message.createdAt(),
                message.published(),
                message.publishedAt());
        OutboxEventEntity saved = jpaRepository.save(entity);
        return toMessage(saved);
    }

    @Override
    public List<OutboxMessage> findUnpublishedOrderByCreatedAt(int limit) {
        return jpaRepository.findUnpublishedOrderByCreatedAt(limit).stream()
                .map(this::toMessage)
                .toList();
    }

    @Override
    public void markAsPublished(String messageId) {
        jpaRepository.markAsPublished(messageId, Instant.now());
    }

    @Override
    public void deletePublishedOlderThan(int olderThanDays) {
        Instant threshold = Instant.now().minus(olderThanDays, ChronoUnit.DAYS);
        jpaRepository.deletePublishedOlderThan(threshold);
    }

    private OutboxMessage toMessage(OutboxEventEntity entity) {
        return new OutboxMessage(
                entity.getId(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getEventType(),
                entity.getPayload(),
                entity.getCreatedAt(),
                entity.isPublished(),
                entity.getPublishedAt());
    }
}
