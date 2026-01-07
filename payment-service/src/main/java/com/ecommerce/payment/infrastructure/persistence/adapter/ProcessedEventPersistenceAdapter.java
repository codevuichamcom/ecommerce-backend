package com.ecommerce.payment.infrastructure.persistence.adapter;

import com.ecommerce.common.kafka.ProcessedEvent;
import com.ecommerce.common.kafka.ProcessedEventRepository;
import com.ecommerce.payment.infrastructure.persistence.entity.ProcessedEventEntity;
import com.ecommerce.payment.infrastructure.persistence.repository.ProcessedEventJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Adapter implementing ProcessedEventRepository using JPA.
 */
@Component
public class ProcessedEventPersistenceAdapter implements ProcessedEventRepository {

    private final ProcessedEventJpaRepository jpaRepository;

    public ProcessedEventPersistenceAdapter(ProcessedEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByEventId(String eventId) {
        return jpaRepository.existsByEventId(eventId);
    }

    @Override
    public ProcessedEvent save(ProcessedEvent event) {
        ProcessedEventEntity entity = new ProcessedEventEntity(
                event.eventId(),
                event.eventType(),
                event.processedAt());
        ProcessedEventEntity saved = jpaRepository.save(entity);
        return new ProcessedEvent(saved.getEventId(), saved.getEventType(), saved.getProcessedAt());
    }

    @Override
    public void deleteOlderThan(int olderThanDays) {
        Instant threshold = Instant.now().minus(olderThanDays, ChronoUnit.DAYS);
        jpaRepository.deleteOlderThan(threshold);
    }
}
