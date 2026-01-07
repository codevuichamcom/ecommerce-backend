package com.ecommerce.order.infrastructure.persistence.adapter;

import com.ecommerce.common.outbox.OutboxMessage;
import com.ecommerce.common.outbox.OutboxRepository;
import com.ecommerce.order.infrastructure.persistence.entity.OutboxEventEntity;
import com.ecommerce.order.infrastructure.persistence.repository.OutboxJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OutboxPersistenceAdapter implements OutboxRepository {

    private final OutboxJpaRepository repository;

    public OutboxPersistenceAdapter(OutboxJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public OutboxMessage save(OutboxMessage message) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setId(UUID.fromString(message.id()));
        entity.setAggregateType(message.aggregateType());
        entity.setAggregateId(message.aggregateId());
        entity.setEventType(message.eventType());
        entity.setPayload(message.payload());
        entity.setCreatedAt(message.createdAt());
        entity.setPublished(message.published());
        entity.setPublishedAt(message.publishedAt());

        OutboxEventEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxMessage> findUnpublishedOrderByCreatedAt(int limit) {
        return repository.findByPublishedFalseOrderByCreatedAtAsc(PageRequest.of(0, limit))
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsPublished(String messageId) {
        repository.findById(UUID.fromString(messageId)).ifPresent(entity -> {
            entity.setPublished(true);
            entity.setPublishedAt(Instant.now());
            repository.save(entity);
        });
    }

    @Override
    @Transactional
    public void deletePublishedOlderThan(int olderThanDays) {
        Instant threshold = Instant.now().minus(olderThanDays, ChronoUnit.DAYS);
        repository.deleteByPublishedTrueAndCreatedAtBefore(threshold);
    }

    private OutboxMessage toDomain(OutboxEventEntity entity) {
        return new OutboxMessage(
                entity.getId().toString(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getEventType(),
                entity.getPayload(),
                entity.getCreatedAt(),
                entity.isPublished(),
                entity.getPublishedAt());
    }
}
