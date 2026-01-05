package com.ecommerce.order.infrastructure.client;

import com.ecommerce.order.application.port.out.InventoryServicePort;
import com.ecommerce.order.infrastructure.config.ServiceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

/**
 * HTTP client for Inventory service using WebClient.
 */
@Component
public class InventoryServiceClient implements InventoryServicePort {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceClient.class);

    private final WebClient webClient;

    public InventoryServiceClient(
            WebClient.Builder webClientBuilder,
            ServiceProperties serviceProperties) {
        this.webClient = webClientBuilder.baseUrl(Objects.requireNonNull(serviceProperties.getInventory().getUrl()))
                .build();
    }

    @Override
    public ReservationResult reserveStock(String productId, int quantity, String reservationReference) {
        log.debug("Reserving {} units of product {} (ref: {})", quantity, productId, reservationReference);

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
                // Parse error message to determine type
                String message = response.data() != null ? response.data().message() : "Unknown error";
                if (message.contains("Insufficient stock")) {
                    return new ReservationResult.InsufficientStock(quantity, 0);
                }
                return new ReservationResult.ServiceUnavailable(message);
            }
        } catch (Exception e) {
            log.error("Failed to reserve stock for product {}: {}", productId, e.getMessage());
            return new ReservationResult.ServiceUnavailable(e.getMessage());
        }
    }

    @Override
    public void releaseStock(String productId, int quantity, String reservationReference) {
        log.debug("Releasing {} units of product {} (ref: {})", quantity, productId, reservationReference);

        try {
            var request = new ReleaseRequest(productId, quantity, reservationReference);

            webClient.post()
                    .uri("/api/inventory/release")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(StockOperationApiResponse.class)
                    .block();

            log.debug("Released stock for product {}", productId);
        } catch (Exception e) {
            log.error("Failed to release stock for product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Failed to release stock", e);
        }
    }

    // Request/Response DTOs
    private record ReserveRequest(String productId, int quantity, String reservationReference) {
    }

    private record ReleaseRequest(String productId, int quantity, String reservationReference) {
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
