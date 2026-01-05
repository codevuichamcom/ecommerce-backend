package com.ecommerce.order.infrastructure.client;

import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.order.application.port.out.ProductServicePort;
import com.ecommerce.order.infrastructure.config.ServiceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * HTTP client for Product service using WebClient.
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

    @Override
    public ProductDetails getProduct(String productId) {
        log.debug("Fetching product details for: {}", productId);

        try {
            var response = webClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(ProductApiResponse.class)
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
            throw new NotFoundException("Product", productId);
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
