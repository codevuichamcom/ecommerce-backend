package com.ecommerce.order.infrastructure.kafka;

import com.ecommerce.common.outbox.OutboxPoller;
import com.ecommerce.common.outbox.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class KafkaConfig {

    @Value("${app.kafka.topics.order-events:order-events}")
    private String orderEventsTopic;

    @Bean
    public OutboxPoller orderOutboxPoller(
            OutboxRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate) {
        return new OutboxPoller(outboxRepository, kafkaTemplate, orderEventsTopic);
    }
}
