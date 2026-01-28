package com.ecommerce.order.infrastructure.client;

import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.order.application.port.out.ProductServicePort;
import com.ecommerce.order.infrastructure.config.ServiceProperties;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final WebClient webClient;

    public ProductServiceClient(
            WebClient.Builder webClientBuilder,
            ServiceProperties serviceProperties) {
        this.webClient = webClientBuilder.baseUrl(Objects.requireNonNull(serviceProperties.getProduct().getUrl()))
                .build();
    }

    /**
     * Phase 4.1: Resilience4j patterns applied
     * - CircuitBreaker: Opens after 50% failure rate in 10 calls
     * - Retry: 3 attempts with exponential backoff (500ms, 1s, 2s)
     * - Bulkhead: Max 25 concurrent calls
     * - TimeLimiter: 3s timeout (replaces manual timeout)
     */
    @Override
    @CircuitBreaker(name = "product-service")
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
            return new ProductDetails(
                    data.id(),
                    data.name(),
                    data.price(),
                    data.currency(),
                    data.available());
        } catch (Exception e) {
            log.error("Failed to fetch product {}: {}", productId, e.getMessage());
            throw e;
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
