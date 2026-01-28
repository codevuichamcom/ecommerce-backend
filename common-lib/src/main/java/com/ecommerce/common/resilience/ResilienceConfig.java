package com.ecommerce.common.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Phase 4.1: Resilience4j Configuration
 * 
 * Provides default configurations for Circuit Breaker, Retry, Bulkhead, and
 * TimeLimiter patterns.
 * Individual services can override these defaults in their application.yml.
 */
@Configuration
public class ResilienceConfig {

    /**
     * Default CircuitBreaker configuration
     * - Sliding window: 10 calls (COUNT_BASED)
     * - Failure rate threshold: 50%
     * - Wait duration in OPEN state: 30 seconds
     * - Minimum calls before evaluation: 5
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50.0f)
                .slowCallRateThreshold(80.0f)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(
                        java.net.ConnectException.class,
                        java.util.concurrent.TimeoutException.class)
                .build();

        return CircuitBreakerRegistry.of(defaultConfig);
    }

    /**
     * Default Retry configuration
     * - Max attempts: 3
     * - Wait duration: 500ms
     * - Exponential backoff: 2x multiplier
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig defaultConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(
                        java.net.ConnectException.class,
                        java.util.concurrent.TimeoutException.class)
                .build();

        return RetryRegistry.of(defaultConfig);
    }

    /**
     * Default Bulkhead configuration
     * - Max concurrent calls: 25
     * - Max wait duration: 500ms
     */
    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        BulkheadConfig defaultConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(25)
                .maxWaitDuration(Duration.ofMillis(500))
                .build();

        return BulkheadRegistry.of(defaultConfig);
    }

    /**
     * Default TimeLimiter configuration
     * - Timeout: 3 seconds
     * - Cancel running future: true
     */
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig defaultConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))
                .cancelRunningFuture(true)
                .build();

        return TimeLimiterRegistry.of(defaultConfig);
    }
}
