package com.ecommerce.inventory.infrastructure.seed.initializer;

import com.ecommerce.inventory.domain.model.Inventory;
import com.ecommerce.inventory.domain.repository.InventoryRepository;
import com.ecommerce.inventory.infrastructure.persistence.repository.InventoryJpaRepository;
import com.ecommerce.inventory.infrastructure.seed.factory.InventoryDataFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Initializes development data for Inventory Service.
 * Runs on startup only in 'dev' profile.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataInitializer implements ApplicationRunner {

    private final InventoryRepository inventoryRepository;
    private final InventoryJpaRepository inventoryJpaRepository;
    private final InventoryDataFactory inventoryDataFactory;
    private final RestTemplate restTemplate;

    private final com.ecommerce.inventory.infrastructure.config.ProductServiceProperties productServiceProperties;

    private static final int BATCH_SIZE = 50;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting inventory data seeding check...");

        if (shouldSkipSeeding()) {
            return;
        }

        seedInventory();
    }

    @Transactional(readOnly = true)
    protected boolean shouldSkipSeeding() {
        long count = inventoryJpaRepository.count();
        if (count > 0) {
            log.info("Inventory already exists (count: {}), skipping seeding.", count);
            return true;
        }
        return false;
    }

    @Transactional
    protected void seedInventory() {
        long startTime = System.currentTimeMillis();

        log.info("No inventory found. Fetching products from Product Service ({}) ...",
                productServiceProperties.getUrl());

        List<String> productIds = fetchProductIds();

        if (productIds.isEmpty()) {
            log.warn("No products found (or Product Service unavailable). Skipping inventory seeding.");
            return;
        }

        log.info("Found {} products. Generating inventory records...", productIds.size());

        List<Inventory> inventories = inventoryDataFactory.generateInventory(productIds);

        log.info("Saving {} inventory records in batches of {}...", inventories.size(), BATCH_SIZE);

        // Save in batches to optimize performance
        int totalSaved = 0;
        for (int i = 0; i < inventories.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, inventories.size());
            List<Inventory> batch = inventories.subList(i, end);

            for (Inventory inventory : batch) {
                inventoryRepository.save(inventory);
            }

            totalSaved += batch.size();
            log.debug("Saved batch: {}/{} inventory records", totalSaved, inventories.size());
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Seeded {} inventory records successfully in {}ms", inventories.size(), duration);

        // Log stats
        long inStock = inventories.stream().filter(i -> i.getAvailableQuantity() >= 10).count();
        long lowStock = inventories.stream().filter(i -> i.getAvailableQuantity() > 0 && i.getAvailableQuantity() < 10)
                .count();
        long outOfStock = inventories.stream().filter(i -> i.getAvailableQuantity() == 0).count();

        log.info("  - In stock: {}", inStock);
        log.info("  - Low stock: {}", lowStock);
        log.info("  - Out of stock: {}", outOfStock);
    }

    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_MS = 3000;
    private static final int PAGE_SIZE = 100;

    private List<String> fetchProductIds() {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("Fetching products from Product Service (attempt {}/{})...", attempt, MAX_RETRIES);
                return fetchProductIdsWithPagination();
            } catch (ResourceAccessException e) {
                log.warn("Could not connect to Product Service at {} (attempt {}/{}). Is it running?",
                        productServiceProperties.getUrl(), attempt, MAX_RETRIES);

                if (attempt < MAX_RETRIES) {
                    try {
                        log.info("Retrying in {}ms...", RETRY_DELAY_MS);
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Retry interrupted");
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching products: {}", e.getMessage(), e);
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("FATAL: Failed to fetch products from Product Service after {} attempts", MAX_RETRIES);
        throw new IllegalStateException(
                "Product Service unavailable at " + productServiceProperties.getUrl() + " after " + MAX_RETRIES
                        + " retries");
    }

    @SuppressWarnings("null")
    private List<String> fetchProductIdsWithPagination() {
        List<String> allProductIds = new ArrayList<>();
        int page = 0;
        boolean hasMore = true;

        while (hasMore) {
            String url = String.format("%s/api/products?size=%d&page=%d",
                    productServiceProperties.getUrl(), PAGE_SIZE, page);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("content")) {
                log.warn("Invalid response from Product Service: {}", body);
                break;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) body.get("content");

            if (content == null) {
                break;
            }

            List<String> pageProductIds = content.stream()
                    .filter(Objects::nonNull)
                    .map(p -> (String) p.get("id"))
                    .collect(Collectors.toList());

            allProductIds.addAll(pageProductIds);

            // Check if there are more pages
            Boolean isLast = (Boolean) body.get("last");
            hasMore = isLast == null || !isLast;
            page++;

            log.debug("Fetched page {} with {} products (total so far: {})",
                    page, pageProductIds.size(), allProductIds.size());
        }

        log.info("Fetched total of {} product IDs from Product Service", allProductIds.size());
        return allProductIds;
    }
}
