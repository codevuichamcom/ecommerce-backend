package com.ecommerce.auth.infrastructure.seed.factory;

import com.ecommerce.auth.domain.model.User;
import com.ecommerce.auth.domain.model.UserId;
import com.ecommerce.common.security.Role;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Factory for generating realistic User entities (Customers).
 */
@Component
@Profile("dev")
@Slf4j
public class UserDataFactory {

    private final Faker faker;
    private final PasswordEncoder passwordEncoder;

    // Hash for 'password123' to avoid re-hashing 120 times (slow)
    private String defaultPasswordHash;

    private static final int TOTAL_USERS = 120;
    private static final double ENABLED_RATIO = 0.95;

    public UserDataFactory(Faker faker, PasswordEncoder passwordEncoder) {
        this.faker = faker;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        try {
            log.debug("Pre-encoding default password for seed data...");
            defaultPasswordHash = passwordEncoder.encode("password123");

            if (defaultPasswordHash == null || defaultPasswordHash.isEmpty()) {
                throw new IllegalStateException("Password encoding resulted in null or empty hash");
            }

            if (!defaultPasswordHash.startsWith("$2a$") && !defaultPasswordHash.startsWith("$2b$")) {
                throw new IllegalStateException(
                        "Password hash doesn't appear to be bcrypt format: " + defaultPasswordHash);
            }

            log.debug("Default password hash initialized successfully");
        } catch (Exception e) {
            log.error("FATAL: Failed to initialize password encoder for seed data", e);
            throw new IllegalStateException("Cannot initialize UserDataFactory - password encoding failed", e);
        }
    }

    public SeededData generateUsers() {
        if (defaultPasswordHash == null) {
            throw new IllegalStateException("Password hash not initialized - call init() first");
        }

        List<User> users = new ArrayList<>();
        int enabledCount = 0;
        int disabledCount = 0;

        for (int i = 0; i < TOTAL_USERS; i++) {
            User user = createUser(i);

            // 5% disabled
            if (i >= TOTAL_USERS * ENABLED_RATIO) {
                user.disable();
                disabledCount++;
            } else {
                enabledCount++;
            }

            users.add(user);
        }

        return new SeededData(users, enabledCount, disabledCount);
    }

    private User createUser(int index) {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String username = (firstName + "." + lastName + index).toLowerCase();
        String email = username + "@example.com";

        Set<Role> roles = Set.of(Role.CUSTOMER);

        // Use the constructor directly to create the user
        // We use generate() for ID
        User user = new User(
                UserId.generate(),
                username,
                email,
                defaultPasswordHash,
                roles);

        return user;
    }
}
