package com.ecommerce.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;

/**
 * Route Configuration for API Gateway.
 * 
 * Defines routing rules to all backend microservices:
 * - Product Service (8081)
 * - Inventory Service (8082)
 * - Order Service (8083)
 * - Payment Service (8084)
 * - Notification Service (8085)
 * - Auth Service (8086) - will be added in Phase 3.2
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class RouteConfig {

        private final RedisRateLimiter userRateLimiter;
        private final RedisRateLimiter anonymousRateLimiter;
        private final KeyResolver userKeyResolver;
        private final EcommerceProperties ecommerceProperties;

        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
                EcommerceProperties.Services services = ecommerceProperties.getServices();
                return builder.routes()
                                // Product Service routes
                                .route("product-service", r -> r
                                                .path("/api/products/**")
                                                .filters(f -> f.requestRateLimiter(c -> c
                                                                .setRateLimiter(userRateLimiter)
                                                                .setKeyResolver(userKeyResolver)))
                                                .uri(services.getProductUrl()))

                                // Inventory Service routes
                                .route("inventory-service", r -> r
                                                .path("/api/inventory/**")
                                                .uri(services.getInventoryUrl()))

                                // Order Service routes
                                .route("order-service", r -> r
                                                .path("/api/orders/**")
                                                .filters(f -> f.requestRateLimiter(c -> c
                                                                .setRateLimiter(userRateLimiter)
                                                                .setKeyResolver(userKeyResolver)))
                                                .uri(services.getOrderUrl()))

                                // Payment Service routes
                                .route("payment-service", r -> r
                                                .path("/api/payments/**")
                                                .filters(f -> f.requestRateLimiter(c -> c
                                                                .setRateLimiter(anonymousRateLimiter)
                                                                .setKeyResolver(userKeyResolver)))
                                                .uri(services.getPaymentUrl()))

                                // Notification Service routes
                                .route("notification-service", r -> r
                                                .path("/api/notifications/**")
                                                .uri(services.getNotificationUrl()))

                                // Auth Service routes
                                .route("auth-service", r -> r
                                                .path("/auth/**")
                                                .filters(f -> f.requestRateLimiter(c -> c
                                                                .setRateLimiter(anonymousRateLimiter)
                                                                .setKeyResolver(userKeyResolver)))
                                                .uri(services.getAuthUrl()))

                                // Health check aggregation - forward to individual services
                                .route("health-product", r -> r
                                                .path("/health/product")
                                                .uri(services.getProductUrl() + "/actuator/health"))

                                .route("health-inventory", r -> r
                                                .path("/health/inventory")
                                                .uri(services.getInventoryUrl() + "/actuator/health"))

                                .route("health-order", r -> r
                                                .path("/health/order")
                                                .uri(services.getOrderUrl() + "/actuator/health"))

                                .route("health-payment", r -> r
                                                .path("/health/payment")
                                                .uri(services.getPaymentUrl() + "/actuator/health"))

                                .route("health-notification", r -> r
                                                .path("/health/notification")
                                                .uri(services.getNotificationUrl() + "/actuator/health"))

                                .route("health-auth", r -> r
                                                .path("/health/auth")
                                                .uri(services.getAuthUrl() + "/actuator/health"))

                                .build();
        }
}
