package com.ecommerce.inventory.infrastructure.seed.config;

import net.datafaker.Faker;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Locale;
import java.util.Random;

/**
 * Configuration for data seeding in development environment.
 * Only active when 'dev' profile is enabled.
 */
@Configuration
@Profile("dev")
public class DataSeedingConfig {

    /**
     * Provides Faker instance with fixed seed for reproducible data.
     */
    @Bean
    public Faker faker() {
        return new Faker(Locale.US, new Random(42));
    }

    /**
     * RestTemplate for calling other services with proper timeout and error handling.
     * Connect timeout: 5s, Read timeout: 10s
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }
}
