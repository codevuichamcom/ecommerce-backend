package com.ecommerce.order.infrastructure.client;

import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.order.application.port.out.ProductServicePort;
import com.ecommerce.order.infrastructure.config.ServiceProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP client for Product service using WebClient.
 * Phase 4.1: Enhanced with Resilience4j patterns (CircuitBreaker, Retry,
 * Bulkhead, TimeLimiter)
 */
@Component
public class ProductServiceClient implements ProductServicePort {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceClient.class);
    private static final String CACHE_KEY_PREFIX = "product:";

    private final WebClient webClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public ProductServiceClient(
            WebClient.Builder webClientBuilder,
            ServiceProperties serviceProperties,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(Objects.requireNonNull(serviceProperties.getProduct().getUrl()))
                .build();
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Phase 4.1: Resilience4j patterns applied
     * Phase 4.2: Fallback with Redis Cache
     * - CircuitBreaker: Opens after 50% failure rate in 10 calls
     * - Retry: 3 attempts with exponential backoff (500ms, 1s, 2s)
     * - Bulkhead: Max 25 concurrent calls
     * - TimeLimiter: 3s timeout (replaces manual timeout)
     */
    @Override
    @CircuitBreaker(name = "product-service", fallbackMethod = "getProductFallback")
    @Retry(name = "product-service")
    @Bulkhead(name = "product-service")
    @TimeLimiter(name = "product-service")
    public ProductDetails getProduct(String productId) {
        log.debug("Fetching product details for: {}", productId);

        try {
            var response = webClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(ProductApiResponse.class)
                    // Manual timeout removed - handled by @TimeLimiter
                    .block();

            if (response == null || !response.success() || response.data() == null) {
                throw new NotFoundException("Product", productId);
            }

            var data = response.data();
            var details = new ProductDetails(
                    data.id(),
                    data.name(),
                    data.price(),
                    data.currency(),
                    data.available());

            // Cache the result for fallback
            cacheProduct(details);

            return details;
        } catch (Exception e) {
            log.error("Failed to fetch product {}: {}", productId, e.getMessage());
            throw e;
        }
    }

    /**
     * Fallback method for getProduct.
     * Strategies:
     * 1. Try to read from Redis cache
     * 2. If missing/error, return simplified placeholder
     */
    public ProductDetails getProductFallback(String productId, Throwable t) {
        log.warn("Fallback triggered for product {}: {}", productId, t.getMessage());

        // 1. Try Cache
        try {
            String json = redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + productId);
            if (json != null) {
                log.info("Returning cached product for {}", productId);
                ProductDetails cached = objectMapper.readValue(json, ProductDetails.class);
                // Return cached version with degraded flag set to true (assuming constructor
                // allows or we use withDegradedFlag logic)
                // Since record is immutable, we reconstruct it with isDegraded = true
                return new ProductDetails(
                        cached.id(),
                        cached.name(),
                        cached.price(),
                        cached.currency(),
                        cached.available(),
                        true // isDegraded
                );
            }
        } catch (Exception e) {
            log.error("Failed to read from fallback cache: {}", e.getMessage());
        }

        // 2. Return Placeholder
        log.warn("Returning placeholder for product {}", productId);
        return new ProductDetails(
                productId,
                "Product Unavailable",
                BigDecimal.ZERO,
                "VND",
                false,
                true // isDegraded
        );
    }

    private void cacheProduct(ProductDetails details) {
        try {
            String json = objectMapper.writeValueAsString(details);
            // Cache for 1 hour (fallback data doesn't need to be infinitely long-lived)
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + details.id(), json, java.time.Duration.ofHours(1));
        } catch (Exception e) {
            log.warn("Failed to cache product {}: {}", details.id(), e.getMessage());
        }
    }

    // Response DTOs matching Product service API
    private record ProductApiResponse(
            boolean success,
            ProductData data) {
    }

    private record ProductData(
            String id,
            String name,
            BigDecimal price,
            String currency,
            boolean available) {
    }
}
