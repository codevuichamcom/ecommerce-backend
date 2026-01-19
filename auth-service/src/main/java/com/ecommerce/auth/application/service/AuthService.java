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

        @Transactional
        public void register(RegisterRequest request) {
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
                User user = userRepository.findByUsername(request.username())
                                .orElseThrow(() -> new ValidationException("Invalid username or password"));

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
