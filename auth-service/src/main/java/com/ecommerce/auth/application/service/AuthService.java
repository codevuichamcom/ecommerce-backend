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
import com.ecommerce.common.security.Role;
import com.ecommerce.common.exception.ConflictException;
import com.ecommerce.common.exception.ValidationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Application service for authentication and user management.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@Service
public class AuthService {

        // SEC-005: Dummy hash for constant-time comparison when user doesn't exist
        private static final String DUMMY_HASH = "$2a$10$dummyhashtopreventtimingattack1234567890123456789012";

        private final UserRepository userRepository;
        private final RefreshTokenRepository refreshTokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;

        public AuthService(UserRepository userRepository,
                        RefreshTokenRepository refreshTokenRepository,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider) {
                this.userRepository = userRepository;
                this.refreshTokenRepository = refreshTokenRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtTokenProvider = jwtTokenProvider;
        }

        /**
         * Validate password strength according to security policy.
         * SEC-003: Password must be at least 8 characters with uppercase, lowercase,
         * number, and special char.
         */
        private void validatePassword(String password) {
                if (password == null || password.length() < 8) {
                        throw new ValidationException("Password must be at least 8 characters long");
                }
                if (!password.matches(".*[A-Z].*")) {
                        throw new ValidationException("Password must contain at least one uppercase letter");
                }
                if (!password.matches(".*[a-z].*")) {
                        throw new ValidationException("Password must contain at least one lowercase letter");
                }
                if (!password.matches(".*[0-9].*")) {
                        throw new ValidationException("Password must contain at least one number");
                }
                if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
                        throw new ValidationException("Password must contain at least one special character");
                }
        }

        /**
         * Validate email format.
         * SEC-003: Email must be valid format.
         */
        private void validateEmail(String email) {
                if (email == null || email.isBlank()) {
                        throw new ValidationException("Email is required");
                }
                // Simple but effective email regex
                String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
                if (!email.matches(emailRegex)) {
                        throw new ValidationException("Invalid email format");
                }
        }

        /**
         * Validate username format.
         * SEC-003: Username must be alphanumeric, 3-30 characters.
         */
        private void validateUsername(String username) {
                if (username == null || username.length() < 3 || username.length() > 30) {
                        throw new ValidationException("Username must be 3-30 characters long");
                }
                if (!username.matches("^[a-zA-Z0-9_]+$")) {
                        throw new ValidationException("Username must contain only letters, numbers, and underscores");
                }
        }

        @Transactional
        public void register(RegisterRequest request) {
                // SEC-003: Validate input before processing
                validateUsername(request.username());
                validateEmail(request.email());
                validatePassword(request.password());

                if (userRepository.existsByUsername(request.username())) {
                        throw new ConflictException("DUPLICATE_USERNAME", "Username already exists");
                }
                if (userRepository.existsByEmail(request.email())) {
                        throw new ConflictException("DUPLICATE_EMAIL", "Email already exists");
                }

                User user = new User(
                                new UserId(UUID.randomUUID()),
                                request.username(),
                                request.email(),
                                passwordEncoder.encode(request.password()),
                                Set.of(Role.CUSTOMER));

                userRepository.save(user);
        }

        @Transactional
        public LoginResponse login(LoginRequest request) {
                // SEC-005: Prevent timing attacks by always performing password comparison
                User user = userRepository.findByUsername(request.username())
                                .orElseGet(() -> {
                                        // Perform dummy password check to maintain constant time
                                        passwordEncoder.matches(request.password(), DUMMY_HASH);
                                        throw new ValidationException("Invalid username or password");
                                });

                if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                        throw new ValidationException("Invalid username or password");
                }

                if (!user.isEnabled()) {
                        throw new ValidationException("User account is disabled");
                }

                user.updateLastLogin();
                userRepository.save(user);

                String accessToken = jwtTokenProvider.generateAccessToken(
                                user.getId().value().toString(),
                                user.getUsername(),
                                user.getRoles());

                String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId().value().toString());

                RefreshToken refreshToken = RefreshToken.create(
                                user.getId(),
                                refreshTokenStr,
                                Instant.now().plusSeconds(jwtTokenProvider.getRefreshTokenValidityInSeconds()));
                refreshTokenRepository.save(refreshToken);

                return new LoginResponse(
                                accessToken,
                                refreshTokenStr,
                                user.getId().value().toString(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getRoles(),
                                jwtTokenProvider.getAccessTokenValidityInSeconds());
        }

        @Transactional
        public LoginResponse refreshToken(String refreshTokenStr) {
                RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                                .orElseThrow(() -> new ValidationException("Invalid refresh token"));

                if (refreshToken.isExpired()) {
                        refreshTokenRepository.deleteByUserId(refreshToken.getUserId());
                        throw new ValidationException("Refresh token expired");
                }

                User user = userRepository.findById(refreshToken.getUserId())
                                .orElseThrow(() -> new ValidationException("User not found"));

                String newAccessToken = jwtTokenProvider.generateAccessToken(
                                user.getId().value().toString(),
                                user.getUsername(),
                                user.getRoles());

                // For simplicity, we keep the same refresh token or could rotate it.
                // Here we just return the same one for now to match the existing domain model.
                return new LoginResponse(
                                newAccessToken,
                                refreshTokenStr,
                                user.getId().value().toString(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getRoles(),
                                jwtTokenProvider.getAccessTokenValidityInSeconds());
        }

        @Transactional
        public void logout(String refreshTokenStr) {
                refreshTokenRepository.findByToken(refreshTokenStr)
                                .ifPresent(token -> refreshTokenRepository.deleteByUserId(token.getUserId()));
        }
}
