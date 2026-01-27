package com.ecommerce.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway Application - Single entry point for all microservices.
 * 
 * Features:
 * - Centralized routing to all backend services
 * - JWT authentication validation (Phase 3.2)
 * - Rate limiting (Phase 3.5)
 * - Request/response logging
 * - Distributed tracing (Phase 3.4)
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@SpringBootApplication
@org.springframework.boot.context.properties.ConfigurationPropertiesScan
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
