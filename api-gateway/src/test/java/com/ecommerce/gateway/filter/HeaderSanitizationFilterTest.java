package com.ecommerce.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HeaderSanitizationFilterTest {

    private HeaderSanitizationFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new HeaderSanitizationFilter();
        chain = mock(GatewayFilterChain.class);
    }

    @Test
    void shouldStripXUserIdHeader() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/products")
                .header("X-User-Id", "hacker123")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
            ServerWebExchange processedExchange = invocation.getArgument(0);
            HttpHeaders headers = processedExchange.getRequest().getHeaders();

            assertThat(headers.containsKey("X-User-Id")).isFalse();
            return Mono.empty();
        });

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void shouldStripAllInternalHeaders() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header("X-User-Id", "fake-id")
                .header("X-User-Name", "fake-name")
                .header("X-User-Roles", "ADMIN")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
            ServerWebExchange processedExchange = invocation.getArgument(0);
            HttpHeaders headers = processedExchange.getRequest().getHeaders();

            assertThat(headers.containsKey("X-User-Id")).isFalse();
            assertThat(headers.containsKey("X-User-Name")).isFalse();
            assertThat(headers.containsKey("X-User-Roles")).isFalse();
            return Mono.empty();
        });

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void shouldPreserveOtherHeaders() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/products")
                .header("X-User-Id", "hacker123")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer valid-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
            ServerWebExchange processedExchange = invocation.getArgument(0);
            HttpHeaders headers = processedExchange.getRequest().getHeaders();

            assertThat(headers.containsKey("X-User-Id")).isFalse();
            assertThat(headers.getFirst("Content-Type")).isEqualTo("application/json");
            assertThat(headers.getFirst("Authorization")).isEqualTo("Bearer valid-token");
            return Mono.empty();
        });

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void shouldPassThroughWhenNoInternalHeaders() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/products")
                .header("Content-Type", "application/json")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void shouldRunBeforeAuthFilter() {
        // Auth filter has order -100
        assertThat(filter.getOrder()).isLessThan(-100);
        assertThat(filter.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 1);
    }
}
