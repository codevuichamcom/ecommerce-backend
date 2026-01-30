package com.ecommerce.notification.infrastructure.config;

import com.ecommerce.common.kafka.ProcessedEventRepository;
import com.ecommerce.notification.infrastructure.persistence.adapter.ProcessedEventPersistenceAdapter;
import com.ecommerce.notification.infrastructure.persistence.repository.ProcessedEventJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdempotencyConfig {

    @Bean
    public ProcessedEventRepository processedEventRepository(ProcessedEventJpaRepository jpaRepository) {
        return new ProcessedEventPersistenceAdapter(jpaRepository);
    }
}
