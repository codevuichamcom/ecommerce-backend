package com.ecommerce.inventory.infrastructure.config;

import com.ecommerce.common.outbox.OutboxEventPublisher;
import com.ecommerce.common.outbox.OutboxRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OutboxConfig {

    @Bean
    public OutboxEventPublisher outboxEventPublisher(OutboxRepository outboxRepository,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return new OutboxEventPublisher(outboxRepository, objectMapper);
    }
}
