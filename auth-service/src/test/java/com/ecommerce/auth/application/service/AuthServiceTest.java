package com.ecommerce.auth.application.service;

import com.ecommerce.auth.application.dto.LoginRequest;
import com.ecommerce.auth.application.dto.LoginResponse;
import com.ecommerce.auth.application.dto.RegisterRequest;
import com.ecommerce.auth.domain.model.RefreshToken;
import com.ecommerce.auth.domain.model.User;
import com.ecommerce.auth.domain.model.UserId;
import com.ecommerce.auth.domain.repository.RefreshTokenRepository;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.infrastructure.security.JwtTokenProvider;
import com.ecommerce.common.exception.ConflictException;
import com.ecommerce.common.exception.ValidationException;
import com.ecommerce.common.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, refreshTokenRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void register_ShouldSaveUser_WhenRequestIsValid() {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        authService.register(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void login_ShouldReturnTokens_WhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        User user = new User(new UserId(UUID.randomUUID()), "testuser", "test@example.com", "encodedHash",
                Set.of(Role.CUSTOMER));

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");

        LoginResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_ShouldThrowException_WhenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        User user = new User(new UserId(UUID.randomUUID()), "testuser", "test@example.com", "encodedHash",
                Set.of(Role.CUSTOMER));

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ValidationException.class);
    }
}
