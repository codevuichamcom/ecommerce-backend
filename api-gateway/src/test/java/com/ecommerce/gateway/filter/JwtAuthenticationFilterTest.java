package com.ecommerce.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.WebFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private WebFilterChain chain;
    private final String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(secretKey);
        chain = mock(WebFilterChain.class);
    }

    @Test
    @SuppressWarnings("null")
    void shouldPassThroughPublicEndpoints() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.post("/auth/login").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result).verifyComplete();
    }

    @Test
    @SuppressWarnings("null")
    void shouldRejectMissingAuthorizationHeader() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @SuppressWarnings("null")
    void shouldAuthenticateValidToken() {
        // Given
        String token = Jwts.builder()
                .subject("user123")
                .claim("username", "testuser")
                .claim("roles", List.of("CUSTOMER"))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
            ServerWebExchange processedExchange = invocation.getArgument(0);
            HttpHeaders headers = processedExchange.getRequest().getHeaders();

            assertThat(headers.getFirst("X-User-Id")).isEqualTo("user123");
            assertThat(headers.getFirst("X-User-Name")).isEqualTo("testuser");
            assertThat(headers.getFirst("X-User-Roles")).isEqualTo("CUSTOMER");

            return Mono.empty();
        });

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result).verifyComplete();
    }

    @Test
    @SuppressWarnings("null")
    void shouldRejectInvalidToken() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
