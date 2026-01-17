package com.ecommerce.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RequestLoggingFilter.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
class RequestLoggingFilterTest {

    private RequestLoggingFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RequestLoggingFilter();
        chain = mock(GatewayFilterChain.class);
    }

    @Test
    void shouldLogRequestAndResponse() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.GET, "/api/products")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getResponse().setStatusCode(HttpStatus.OK);

        when(chain.filter(any(ServerWebExchange.class)))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        // Verify start time was recorded
        assertThat(exchange.getAttributes().get("startTime")).isNotNull();
    }

    @Test
    void shouldHaveHighestPrecedence() {
        // When
        int order = filter.getOrder();

        // Then
        assertThat(order).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void shouldHandleNullStartTime() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.POST, "/api/orders")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getResponse().setStatusCode(HttpStatus.CREATED);

        // Remove start time to simulate edge case
        exchange.getAttributes().remove("startTime");

        when(chain.filter(any(ServerWebExchange.class)))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then - should not throw exception
        StepVerifier.create(result)
                .verifyComplete();
    }
}
