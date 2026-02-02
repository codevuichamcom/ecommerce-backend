package com.ecommerce.product.infrastructure.seed.initializer;

import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.repository.ProductRepository;
import com.ecommerce.product.infrastructure.seed.factory.ProductDataFactory;
import com.ecommerce.product.infrastructure.seed.factory.SeededData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.ecommerce.product.infrastructure.persistence.repository.ProductJpaRepository;

/**
 * Initializes development data for Product Service.
 * Runs on startup only in 'dev' profile.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final ProductJpaRepository productJpaRepository;
    private final ProductDataFactory productDataFactory;

    private static final int BATCH_SIZE = 50;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting product data seeding check...");

        if (shouldSkipSeeding()) {
            return;
        }

        seedProducts();
    }

    @Transactional(readOnly = true)
    protected boolean shouldSkipSeeding() {
        long count = productJpaRepository.count();
        if (count > 0) {
            log.info("Products already exist (count: {}), skipping seeding.", count);
            return true;
        }
        return false;
    }

    @Transactional
    protected void seedProducts() {
        long startTime = System.currentTimeMillis();

        log.info("No products found. Generating seed data...");
        SeededData seededData = productDataFactory.generateProducts();
        List<Product> products = seededData.products();

        log.info("Saving {} products in batches of {}...", products.size(), BATCH_SIZE);

        // Save in batches to optimize performance
        int totalSaved = 0;
        for (int i = 0; i < products.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, products.size());
            List<Product> batch = products.subList(i, end);

            for (Product product : batch) {
                productRepository.save(product);
            }

            totalSaved += batch.size();
            log.debug("Saved batch: {}/{} products", totalSaved, products.size());
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Seeded {} products successfully in {}ms", products.size(), duration);
        log.info("  - Active: {}", seededData.activeCount());
        log.info("  - Inactive: {}", seededData.inactiveCount());
        log.info("  - Draft: {}", seededData.draftCount());
        log.info("  - Discontinued: {}", seededData.discontinuedCount());

        if (!products.isEmpty()) {
            Product sample = products.get(0);
            log.info("  - Sample product: {} ({}) - {}", sample.getName(), sample.getSku(), sample.getPrice());
        }
    }
}
