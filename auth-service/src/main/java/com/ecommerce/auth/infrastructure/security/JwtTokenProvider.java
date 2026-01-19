package com.ecommerce.auth.infrastructure.security;

import com.ecommerce.common.security.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT Token Provider for generating and validating JWT tokens.
 * 
 * Uses JJWT library with HS256 algorithm.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate access token with user claims.
     */
    public String generateAccessToken(String userId, String username, Set<Role> roles) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.getAccessTokenValiditySeconds(), ChronoUnit.SECONDS);

        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("roles", roles.stream().map(Enum::name).collect(Collectors.toList()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generate refresh token.
     */
    public String generateRefreshToken(String userId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.getRefreshTokenValiditySeconds(), ChronoUnit.SECONDS);

        return Jwts.builder()
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * CQ-003: Token validation result with detailed error information.
     */
    public record TokenValidationResult(boolean isValid, String errorReason) {
        public static TokenValidationResult success() {
            return new TokenValidationResult(true, null);
        }

        public static TokenValidationResult failure(String reason) {
            return new TokenValidationResult(false, reason);
        }
    }

    /**
     * Validate token signature and expiration with detailed error information.
     * CQ-003: Returns validation result instead of boolean for better error
     * handling.
     */
    public TokenValidationResult validateTokenDetailed(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return TokenValidationResult.success();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return TokenValidationResult.failure("Token expired");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            return TokenValidationResult.failure("Malformed token");
        } catch (io.jsonwebtoken.security.SignatureException e) {
            return TokenValidationResult.failure("Invalid signature");
        } catch (Exception e) {
            return TokenValidationResult.failure("Invalid token: " + e.getMessage());
        }
    }

    /**
     * Validate token signature and expiration (legacy method for backward
     * compatibility).
     * 
     * @deprecated Use {@link #validateTokenDetailed(String)} for better error
     *             information
     */
    @Deprecated
    public boolean validateToken(String token) {
        return validateTokenDetailed(token).isValid();
    }

    /**
     * Extract user ID from token.
     */
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Extract username from token.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("username", String.class);
    }

    /**
     * Extract roles from token.
     */
    @SuppressWarnings("unchecked")
    public Set<Role> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<String> roleNames = (List<String>) claims.get("roles", List.class);
        return roleNames.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }

    public long getAccessTokenValidityInSeconds() {
        return jwtProperties.getAccessTokenValiditySeconds();
    }

    public long getRefreshTokenValidityInSeconds() {
        return jwtProperties.getRefreshTokenValiditySeconds();
    }
}
