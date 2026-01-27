package com.ecommerce.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    /**
     * Rate limit by User ID (authenticated) or IP address (anonymous)
     */
    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                return Mono.just(userId);
            }
            // Fallback to IP address for anonymous users
            // SEC-004: Handle null remote address gracefully (e.g., from proxy)
            return Mono.justOrEmpty(exchange.getRequest().getRemoteAddress())
                    .map(addr -> addr.getAddress().getHostAddress())
                    .defaultIfEmpty("unknown");
        };
    }

    @Bean
    public KeyResolver ipKeyResolver() {
        // SEC-004: Handle null remote address gracefully
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .defaultIfEmpty("unknown");
    }

    @Bean
    @Primary
    public RedisRateLimiter userRateLimiter() {
        return new RedisRateLimiter(50, 100); // 50 replenish rate, 100 burst capacity
    }

    @Bean
    public RedisRateLimiter anonymousRateLimiter() {
        return new RedisRateLimiter(10, 20); // 10 replenish rate, 20 burst capacity
    }
}
