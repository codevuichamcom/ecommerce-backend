package com.ecommerce.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.ecommerce.gateway.config.EcommerceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Global filter for JWT authentication.
 * Validates JWT from Authorization header and forwards user context to
 * downstream services.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Component
public class JwtAuthenticationFilter implements WebFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final SecretKey secretKey;
    private final List<String> publicEndpoints = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/api/products", // GET products is public
            "/actuator/health",
            "/actuator/prometheus");

    public JwtAuthenticationFilter(EcommerceProperties properties) {
        String secret = properties.getJwtSecret();
        // SEC-001: Validate JWT secret key length (HS256 requires at least 256 bits =
        // 32 bytes)
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 bytes for HS256. Current length: " +
                            (secret != null ? secret.getBytes(StandardCharsets.UTF_8).length : 0));
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Sanitize header value to prevent HTTP header injection attacks.
     * Removes carriage return and newline characters.
     * 
     * @param value the header value to sanitize
     * @return sanitized header value
     */
    private String sanitizeHeaderValue(String value) {
        if (value == null) {
            return "";
        }
        // SEC-002: Remove \r and \n to prevent header injection
        return value.replaceAll("[\\r\\n]", "");
    }

    private static final java.util.Map<String, List<String>> ROLE_PROTECTED_ENDPOINTS = java.util.Map.of(
            "/api/orders", List.of("CUSTOMER", "ADMIN"),
            "/api/payments", List.of("SERVICE", "ADMIN"),
            "/api/inventory", List.of("ADMIN", "SERVICE"),
            "/api/products", List.of("ADMIN", "SERVICE"), // Only for POST/PUT/DELETE
            "/actuator", List.of("ADMIN") // Secure actuator
    );

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Check for OPTIONS request (CORS preflight)
        if (org.springframework.http.HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        // Check if endpoint is public
        if (isPublicEndpoint(path)) {
            // Special case: GET /api/products is public, but others need ADMIN/SERVICE
            if (path.startsWith("/api/products") && !request.getMethod().name().equals("GET")) {
                // fall through to token validation
            } else {
                return chain.filter(exchange);
            }
        }

        // Check Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Extract info from token
            String userId = claims.getSubject();
            String username = claims.get("username", String.class);
            List<?> roles = claims.get("roles", List.class);

            // Role-based Authorization check
            if (!isAuthorized(path, request.getMethod().name(), roles)) {
                logger.warn("User {} unauthorized for {} {}", username, request.getMethod(), path);
                return onError(exchange, "Insufficient permissions", HttpStatus.FORBIDDEN);
            }

            // Forward info to downstream services via headers
            // SEC-002: Sanitize all header values to prevent injection attacks
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", sanitizeHeaderValue(userId))
                    .header("X-User-Name", sanitizeHeaderValue(username))
                    .header("X-User-Roles",
                            sanitizeHeaderValue(String.join(",", roles.stream().map(Object::toString).toList())))
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isAuthorized(String path, String method, List<?> userRoles) {
        if (userRoles == null)
            return false;

        List<String> userRolesStr = userRoles.stream().map(Object::toString).toList();

        // Find matching protection rule
        for (java.util.Map.Entry<String, List<String>> entry : ROLE_PROTECTED_ENDPOINTS.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                // For Products, only POST/PUT/DELETE are protected
                if (entry.getKey().equals("/api/products") && method.equals("GET")) {
                    return true;
                }

                // Check if user has at least one of the required roles
                return entry.getValue().stream().anyMatch(userRolesStr::contains);
            }
        }

        // Default: If not specifically protected but requires token, allow any valid
        // token
        return true;
    }

    private boolean isPublicEndpoint(String path) {
        return publicEndpoints.stream().anyMatch(path::startsWith);
    }

    @NonNull
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return java.util.Objects.requireNonNull(exchange.getResponse().setComplete(),
                "Response complete Mono must not be null");
    }

    @Override
    public int getOrder() {
        return -100; // Run before other filters
    }
}
