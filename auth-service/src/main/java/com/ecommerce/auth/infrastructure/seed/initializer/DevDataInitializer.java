package com.ecommerce.auth.infrastructure.seed.initializer;

import com.ecommerce.auth.domain.model.User;
import com.ecommerce.auth.domain.repository.UserRepository;
import com.ecommerce.auth.infrastructure.persistence.repository.JpaUserRepository;
import com.ecommerce.auth.infrastructure.seed.factory.SeededData;
import com.ecommerce.auth.infrastructure.seed.factory.UserDataFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Initializes development data for Auth Service (Customers).
 * Runs on startup only in 'dev' profile.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataInitializer implements ApplicationRunner {

    // Domain repository for saving business objects
    private final UserRepository userRepository;

    // Infrastructure repository for efficient counts
    private final JpaUserRepository jpaUserRepository;

    private final UserDataFactory userDataFactory;

    private static final int BATCH_SIZE = 50;
    private static final int SYSTEM_USERS_COUNT = 2; // admin + service-account
    private static final int EXPECTED_CUSTOMER_COUNT = 120;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting user data seeding check...");

        if (shouldSkipSeeding()) {
            return;
        }

        seedUsers();
    }

    @Transactional(readOnly = true)
    protected boolean shouldSkipSeeding() {
        long count = jpaUserRepository.count();
        // If we have more than system users, assume customers already seeded
        if (count > SYSTEM_USERS_COUNT) {
            log.info("Users already exist (count: {}), skipping customer seeding.", count);
            return true;
        }
        return false;
    }

    @Transactional
    protected void seedUsers() {
        long startTime = System.currentTimeMillis();

        log.info("Only system users found. Generating {} customer seed data...", EXPECTED_CUSTOMER_COUNT);
        SeededData seededData = userDataFactory.generateUsers();
        List<User> users = seededData.users();

        log.info("Saving {} customer users in batches of {}...", users.size(), BATCH_SIZE);

        // Save in batches to optimize performance
        int totalSaved = 0;
        for (int i = 0; i < users.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, users.size());
            List<User> batch = users.subList(i, end);

            for (User user : batch) {
                userRepository.save(user);
            }

            totalSaved += batch.size();
            log.debug("Saved batch: {}/{} users", totalSaved, users.size());
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Seeded {} customer users successfully in {}ms", users.size(), duration);
        log.info("  - Default password for all: password123");

        if (!users.isEmpty()) {
            User sample = users.get(0);
            log.info("  - Sample user: {} ({})", sample.getUsername(), sample.getEmail());
        }
        log.info("  - Enabled: {}, Disabled: {}", seededData.enabledCount(), seededData.disabledCount());
    }
}
