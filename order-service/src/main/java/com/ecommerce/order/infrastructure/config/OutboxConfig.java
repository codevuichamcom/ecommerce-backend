package com.ecommerce.order.infrastructure.config;

import com.ecommerce.common.outbox.OutboxEventPublisher;
import com.ecommerce.common.outbox.OutboxRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OutboxConfig {

    @Bean
    public OutboxEventPublisher outboxEventPublisher(OutboxRepository outboxRepository) {
        return new OutboxEventPublisher(outboxRepository);
    }
}
