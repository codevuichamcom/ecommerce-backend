package com.ecommerce.order.infrastructure.persistence.adapter;

import com.ecommerce.common.kafka.ProcessedEvent;
import com.ecommerce.common.kafka.ProcessedEventRepository;
import com.ecommerce.order.infrastructure.persistence.entity.ProcessedEventEntity;
import com.ecommerce.order.infrastructure.persistence.repository.ProcessedEventJpaRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProcessedEventPersistenceAdapter implements ProcessedEventRepository {

    private final ProcessedEventJpaRepository jpaRepository;

    @Override
    public boolean existsByEventId(String eventId) {
        if (eventId == null) {
            return false;
        }
        return jpaRepository.existsById(eventId);
    }

    @Override
    public ProcessedEvent save(ProcessedEvent event) {
        if (event == null) {
            return null;
        }
        ProcessedEventEntity entity = ProcessedEventEntity.builder()
                .eventId(event.eventId())
                .processedAt(event.processedAt())
                .build();

        jpaRepository.save(java.util.Objects.requireNonNull(entity));
        return event;
    }

    @Override
    public void deleteOlderThan(int olderThanDays) {
        // Implementation for cleanup if needed
    }
}
