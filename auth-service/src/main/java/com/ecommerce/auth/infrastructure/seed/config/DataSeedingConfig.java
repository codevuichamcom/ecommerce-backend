package com.ecommerce.auth.infrastructure.seed.config;

import net.datafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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
     * Seed value: 42 (ensures same data generated every time)
     */
    @Bean
    public Faker faker() {
        return new Faker(Locale.US, new Random(42));
    }
}
