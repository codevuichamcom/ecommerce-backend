package com.ecommerce.auth.infrastructure.security;

import com.ecommerce.common.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long accessTokenValidity = 3600;
    private final long refreshTokenValidity = 86400;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secretKey, accessTokenValidity, refreshTokenValidity);
    }

    @Test
    void generateAccessToken_ShouldCreateValidToken() {
        String userId = "user123";
        String username = "testuser";
        Set<Role> roles = Set.of(Role.CUSTOMER);

        String token = jwtTokenProvider.generateAccessToken(userId, username, roles);

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(username);
        assertThat(jwtTokenProvider.getRolesFromToken(token)).containsExactly(Role.CUSTOMER);
    }

    @Test
    void generateRefreshToken_ShouldCreateValidToken() {
        String userId = "user123";

        String token = jwtTokenProvider.generateRefreshToken(userId);

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token.structure";
        assertThat(jwtTokenProvider.validateToken(invalidToken)).isFalse();
    }
}
