package com.ecommerce.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
public class RouteConfig {

        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
                return builder.routes()
                                // Product Service routes
                                .route("product-service", r -> r
                                                .path("/api/products/**")
                                                .uri("http://localhost:8081"))

                                // Inventory Service routes
                                .route("inventory-service", r -> r
                                                .path("/api/inventory/**")
                                                .uri("http://localhost:8082"))

                                // Order Service routes
                                .route("order-service", r -> r
                                                .path("/api/orders/**")
                                                .uri("http://localhost:8083"))

                                // Payment Service routes
                                .route("payment-service", r -> r
                                                .path("/api/payments/**")
                                                .uri("http://localhost:8084"))

                                // Notification Service routes
                                .route("notification-service", r -> r
                                                .path("/api/notifications/**")
                                                .uri("http://localhost:8085"))

                                // Auth Service routes
                                .route("auth-service", r -> r
                                                .path("/auth/**")
                                                .uri("http://localhost:8086"))

                                // Health check aggregation - forward to individual services
                                .route("health-product", r -> r
                                                .path("/health/product")
                                                .uri("http://localhost:8081/actuator/health"))

                                .route("health-inventory", r -> r
                                                .path("/health/inventory")
                                                .uri("http://localhost:8082/actuator/health"))

                                .route("health-order", r -> r
                                                .path("/health/order")
                                                .uri("http://localhost:8083/actuator/health"))

                                .route("health-payment", r -> r
                                                .path("/health/payment")
                                                .uri("http://localhost:8084/actuator/health"))

                                .route("health-notification", r -> r
                                                .path("/health/notification")
                                                .uri("http://localhost:8085/actuator/health"))

                                .route("health-auth", r -> r
                                                .path("/health/auth")
                                                .uri("http://localhost:8086/actuator/health"))

                                .build();
        }
}
