package com.ecommerce.inventory.infrastructure.persistence.adapter;

import com.ecommerce.common.kafka.ProcessedEvent;
import com.ecommerce.common.kafka.ProcessedEventRepository;
import com.ecommerce.inventory.infrastructure.persistence.entity.ProcessedEventEntity;
import com.ecommerce.inventory.infrastructure.persistence.repository.ProcessedEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
public class ProcessedEventPersistenceAdapter implements ProcessedEventRepository {

    private final ProcessedEventJpaRepository jpaRepository;

    @Override
    public boolean existsByEventId(String eventId) {
        return jpaRepository.existsById(eventId);
    }

    @Override
    public ProcessedEvent save(ProcessedEvent event) {
        ProcessedEventEntity entity = ProcessedEventEntity.builder()
                .eventId(event.eventId())
                .processedAt(event.processedAt())
                .build();

        jpaRepository.save(entity);
        return event;
    }

    @Override
    public void deleteOlderThan(int olderThanDays) {
        // Implementation for cleanup if needed
    }
}
