package com.ecommerce.order.infrastructure.client;

import com.ecommerce.order.application.port.out.InventoryServicePort;
import com.ecommerce.order.infrastructure.config.ServiceProperties;
import com.ecommerce.order.infrastructure.persistence.entity.RetryTaskEntity;
import com.ecommerce.order.infrastructure.persistence.repository.RetryTaskJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;
import java.util.UUID;

/**
 * HTTP client for Inventory service using WebClient.
 * Phase 4.1: Enhanced with Resilience4j patterns (CircuitBreaker, Retry,
 * Bulkhead, TimeLimiter)
 */
@Component
public class InventoryServiceClient implements InventoryServicePort {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceClient.class);

    private final WebClient webClient;
    private final RetryTaskJpaRepository retryTaskRepository;
    private final ObjectMapper objectMapper;

    public InventoryServiceClient(
            WebClient.Builder webClientBuilder,
            ServiceProperties serviceProperties,
            RetryTaskJpaRepository retryTaskRepository,
            ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(Objects.requireNonNull(serviceProperties.getInventory().getUrl()))
                .build();
        this.retryTaskRepository = retryTaskRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Phase 4.1: Resilience4j patterns applied
     * Phase 4.2: Fallback with Retry Queue
     * - CircuitBreaker: Opens after 40% failure rate (more sensitive for inventory)
     * - Retry: 4 attempts with exponential backoff
     * - Bulkhead: Max 50 concurrent calls
     * - TimeLimiter: 5s timeout (longer for inventory operations)
     */
    @Override
    @CircuitBreaker(name = "inventory-service", fallbackMethod = "reserveStockFallback")
    @Retry(name = "inventory-service")
    @Bulkhead(name = "inventory-service")
    @TimeLimiter(name = "inventory-service")
    public ReservationResult reserveStock(String productId, int quantity, String reservationReference) {
        log.debug("Reserving {} units of product {} (ref: {})", quantity, productId, reservationReference);

        try {
            var request = new ReserveRequest(productId, quantity, reservationReference);

            var response = webClient.post()
                    .uri("/api/inventory/reserve")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(StockOperationApiResponse.class)
                    // Manual timeout removed - handled by @TimeLimiter
                    .block();

            if (response == null) {
                return new ReservationResult.ServiceUnavailable("No response from inventory service");
            }

            if (response.success() && response.data() != null && response.data().success()) {
                return new ReservationResult.Success(
                        response.data().availableQuantity(),
                        response.data().reservedQuantity());
            } else {
                // Parse error message to determine type
                String message = response.data() != null ? response.data().message() : "Unknown error";
                if (message.contains("Insufficient stock")) {
                    return new ReservationResult.InsufficientStock(quantity, 0);
                }
                return new ReservationResult.ServiceUnavailable(message);
            }
        } catch (Exception e) {
            log.error("Failed to reserve stock for product {}: {}", productId, e.getMessage());
            throw e; // Rethrow to trigger fallback
        }
    }

    public ReservationResult reserveStockFallback(String productId, int quantity, String reservationReference,
            Throwable t) {
        log.warn("Fallback triggered for reserveStock: {}", t.getMessage());
        try {
            var request = new ReserveRequest(productId, quantity, reservationReference);
            String payload = objectMapper.writeValueAsString(request);

            var task = new RetryTaskEntity(
                    UUID.randomUUID(),
                    "RESERVE_STOCK",
                    payload,
                    10 // Max 10 attempts for background retry
            );

            retryTaskRepository.save(task);
            log.info("Queued reserve stock task: {}", task.getId());

            return new ReservationResult.Pending(task.getId().toString());
        } catch (Exception e) {
            log.error("Failed to queue retry task: {}", e.getMessage());
            return new ReservationResult.ServiceUnavailable("Service unavailable and fallback queue failed");
        }
    }

    /**
     * Phase 4.1: Resilience4j patterns applied for release operation
     */
    @Override
    @CircuitBreaker(name = "inventory-service", fallbackMethod = "releaseStockFallback")
    @Retry(name = "inventory-service")
    @Bulkhead(name = "inventory-service")
    @TimeLimiter(name = "inventory-service")
    public void releaseStock(String productId, int quantity, String reservationReference) {
        log.debug("Releasing {} units of product {} (ref: {})", quantity, productId, reservationReference);

        try {
            var request = new ReleaseRequest(productId, quantity, reservationReference);

            webClient.post()
                    .uri("/api/inventory/release")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(StockOperationApiResponse.class)
                    // Manual timeout removed - handled by @TimeLimiter
                    .block();

            log.debug("Released stock for product {}", productId);
        } catch (Exception e) {
            log.error("Failed to release stock for product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Failed to release stock", e);
        }
    }

    public void releaseStockFallback(String productId, int quantity, String reservationReference, Throwable t) {
        log.warn("Fallback triggered for releaseStock: {}", t.getMessage());
        try {
            var request = new ReleaseRequest(productId, quantity, reservationReference);
            String payload = objectMapper.writeValueAsString(request);

            var task = new RetryTaskEntity(
                    UUID.randomUUID(),
                    "RELEASE_STOCK",
                    payload,
                    10 // Max 10 attempts
            );

            retryTaskRepository.save(task);
            log.info("Queued release stock task: {}", task.getId());
        } catch (Exception e) {
            log.error("Failed to queue release retry task: {}", e.getMessage());
        }
    }

    /**
     * Direct method for RetryTaskProcessor to execute retries without triggering
     * fallback
     */
    public ReservationResult reserveStockDirect(String productId, int quantity, String reservationReference) {
        try {
            var request = new ReserveRequest(productId, quantity, reservationReference);

            var response = webClient.post()
                    .uri("/api/inventory/reserve")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(StockOperationApiResponse.class)
                    .block();

            if (response == null) {
                return new ReservationResult.ServiceUnavailable("No response from inventory service");
            }

            if (response.success() && response.data() != null && response.data().success()) {
                return new ReservationResult.Success(
                        response.data().availableQuantity(),
                        response.data().reservedQuantity());
            } else {
                String message = response.data() != null ? response.data().message() : "Unknown error";
                if (message.contains("Insufficient stock")) {
                    return new ReservationResult.InsufficientStock(quantity, 0);
                }
                return new ReservationResult.ServiceUnavailable(message);
            }
        } catch (Exception e) {
            log.error("Retry attempt failed for product {}: {}", productId, e.getMessage());
            return new ReservationResult.ServiceUnavailable(e.getMessage());
        }
    }

    // Request/Response DTOs
    public record ReserveRequest(String productId, int quantity, String reservationReference) {
    }

    public record ReleaseRequest(String productId, int quantity, String reservationReference) {
    }

    private record StockOperationApiResponse(
            boolean success,
            StockOperationData data) {
    }

    private record StockOperationData(
            boolean success,
            String inventoryId,
            int availableQuantity,
            int reservedQuantity,
            String message) {
    }
}
