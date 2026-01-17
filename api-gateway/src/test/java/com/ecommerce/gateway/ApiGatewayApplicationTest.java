package com.ecommerce.gateway;

import com.ecommerce.gateway.config.RouteConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke tests for API Gateway configuration.
 * 
 * Full integration tests will be added in Phase 3.6.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
class ApiGatewayApplicationTest {

    @Test
    void shouldInstantiateRouteConfig() {
        // Given & When
        RouteConfig routeConfig = new RouteConfig();

        // Then
        assertThat(routeConfig).isNotNull();
    }
}
