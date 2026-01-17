package com.ecommerce.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter for logging all incoming requests and outgoing responses.
 * 
 * Logs:
 * - Request method, URI, and headers
 * - Response status code and processing time
 * - Correlation ID for distributed tracing
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String START_TIME_ATTR = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Record start time
        exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());

        // Log incoming request
        log.info("Incoming request: {} {} from {}",
                request.getMethod(),
                request.getURI().getPath(),
                request.getRemoteAddress());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            Long startTime = exchange.getAttribute(START_TIME_ATTR);

            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;

                log.info("Outgoing response: {} {} - Status: {} - Duration: {}ms",
                        request.getMethod(),
                        request.getURI().getPath(),
                        response.getStatusCode(),
                        duration);
            }
        }));
    }

    @Override
    public int getOrder() {
        // Execute first in the filter chain
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
