package com.ecommerce.inventory.infrastructure.config;

import com.ecommerce.common.kafka.ProcessedEventRepository;
import com.ecommerce.inventory.infrastructure.persistence.adapter.ProcessedEventPersistenceAdapter;
import com.ecommerce.inventory.infrastructure.persistence.repository.ProcessedEventJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdempotencyConfig {

    @Bean
    public ProcessedEventRepository processedEventRepository(ProcessedEventJpaRepository jpaRepository) {
        return new ProcessedEventPersistenceAdapter(jpaRepository);
    }
}
