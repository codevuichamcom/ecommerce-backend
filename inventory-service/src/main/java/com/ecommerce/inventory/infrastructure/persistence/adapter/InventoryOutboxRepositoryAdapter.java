package com.ecommerce.inventory.infrastructure.persistence.adapter;

import com.ecommerce.common.outbox.OutboxMessage;
import com.ecommerce.common.outbox.OutboxRepository;
import com.ecommerce.inventory.domain.repository.InventoryOutboxRepository;
import com.ecommerce.inventory.infrastructure.persistence.entity.OutboxEventEntity;
import org.springframework.stereotype.Component;

@Component
public class InventoryOutboxRepositoryAdapter implements OutboxRepository {

    private final InventoryOutboxRepository repository;

    public InventoryOutboxRepositoryAdapter(InventoryOutboxRepository repository) {
        this.repository = repository;
    }

    @Override
    public OutboxMessage save(OutboxMessage message) {
        OutboxEventEntity entity = toEntity(message);
        OutboxEventEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public java.util.List<OutboxMessage> findUnpublishedForUpdate(int limit) {
        return repository.findUnpublishedForUpdate(limit).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void markAsPublished(String messageId) {
        repository.findById(messageId).ifPresent(entity -> {
            entity.setPublished(true);
            entity.setPublishedAt(java.time.Instant.now());
            repository.save(entity);
        });
    }

    @Override
    public void deletePublishedOlderThan(int olderThanDays) {
        java.time.Instant cutoff = java.time.Instant.now().minus(olderThanDays, java.time.temporal.ChronoUnit.DAYS);
        repository.deletePublishedBefore(cutoff);
    }

    private OutboxEventEntity toEntity(OutboxMessage message) {
        return new OutboxEventEntity(
                message.id(),
                message.aggregateType(),
                message.aggregateId(),
                message.eventType(),
                message.payload(),
                message.createdAt(),
                message.published(),
                message.publishedAt());
    }

    private OutboxMessage toDomain(OutboxEventEntity entity) {
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
