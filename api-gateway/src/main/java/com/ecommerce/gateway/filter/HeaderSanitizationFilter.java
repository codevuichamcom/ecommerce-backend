package com.ecommerce.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Global filter that sanitizes incoming requests by removing internal headers.
 * This prevents external clients from spoofing user identity headers.
 * 
 * <p>
 * Must run BEFORE {@link JwtAuthenticationFilter} to ensure headers are
 * stripped before any authentication logic executes.
 * </p>
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Component
public class HeaderSanitizationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(HeaderSanitizationFilter.class);

    private static final List<String> INTERNAL_HEADERS = List.of(
            "X-User-Id",
            "X-User-Name",
            "X-User-Roles");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest originalRequest = exchange.getRequest();

        // Check if any internal headers are present
        boolean hasInternalHeaders = INTERNAL_HEADERS.stream()
                .anyMatch(header -> originalRequest.getHeaders().containsKey(header));

        if (hasInternalHeaders) {
            logger.warn("Stripping internal headers from external request: {}",
                    originalRequest.getPath().value());

            ServerHttpRequest sanitizedRequest = originalRequest.mutate()
                    .headers(headers -> INTERNAL_HEADERS.forEach(headers::remove))
                    .build();

            return chain.filter(exchange.mutate().request(sanitizedRequest).build());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Run after logging (HIGHEST_PRECEDENCE) but before auth (-100)
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
