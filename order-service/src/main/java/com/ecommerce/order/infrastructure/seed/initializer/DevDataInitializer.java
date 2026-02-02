package com.ecommerce.order.infrastructure.seed.initializer;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.order.infrastructure.persistence.repository.OrderJpaRepository;
import com.ecommerce.order.infrastructure.seed.factory.OrderDataFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.stream.Collectors;

/**
 * Initializes development data for Order Service.
 * Runs on startup only in 'dev' profile.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataInitializer implements ApplicationRunner {

    private final OrderRepository orderRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final OrderDataFactory orderDataFactory;
    private final RestTemplate restTemplate;

    @Value("${services.product.url:http://localhost:8081}")
    private String productServiceUrl;

    @Value("${services.auth.url:http://localhost:8086}")
    private String authServiceUrl;

    private static final int BATCH_SIZE = 50;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting order data seeding check...");

        if (shouldSkipSeeding()) {
            return;
        }

        seedOrders();
    }

    @Transactional(readOnly = true)
    protected boolean shouldSkipSeeding() {
        long count = orderJpaRepository.count();
        if (count > 0) {
            log.info("Orders already exist (count: {}), skipping seeding.", count);
            return true;
        }
        return false;
    }

    @Transactional
    protected void seedOrders() {
        long startTime = System.currentTimeMillis();

        log.info("No orders found. Fetching data from Product and Auth Services...");

        // Fetch products
        log.info("Fetching products from Product Service ({})...", productServiceUrl);
        List<String> productIds = fetchProductIds();

        if (productIds.isEmpty()) {
            log.warn("No products found (or Product Service unavailable). Skipping order seeding.");
            return;
        }

        log.info("Found {} products.", productIds.size());

        // Fetch customers
        log.info("Fetching customers from Auth Service ({})...", authServiceUrl);
        List<String> customerIds = fetchCustomerIds();

        if (customerIds.isEmpty()) {
            log.warn("No customers found (or Auth Service unavailable). Skipping order seeding.");
            return;
        }

        log.info("Found {} customers.", customerIds.size());
        log.info("Generating order records...");

        List<Order> orders = orderDataFactory.generateOrders(productIds, customerIds);

        log.info("Saving {} order records in batches of {}...", orders.size(), BATCH_SIZE);

        // Save in batches to optimize performance
        int totalSaved = 0;
        for (int i = 0; i < orders.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, orders.size());
            List<Order> batch = orders.subList(i, end);

            for (Order order : batch) {
                orderRepository.save(order);
            }

            totalSaved += batch.size();
            log.debug("Saved batch: {}/{} orders", totalSaved, orders.size());
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Seeded {} order records successfully in {}ms", orders.size(), duration);
    }

    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_MS = 3000;
    private static final int PAGE_SIZE = 100;

    private List<String> fetchProductIds() {
        return fetchIdsWithRetry("Product Service", productServiceUrl, "/api/products", null);
    }

    private List<String> fetchCustomerIds() {
        return fetchIdsWithRetry("Auth Service", authServiceUrl, "/api/users", this::filterCustomerIds);
    }

    private List<String> filterCustomerIds(List<Map<String, Object>> content) {
        return content.stream()
                .filter(u -> {
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) u.get("roles");
                    return roles != null && roles.contains("CUSTOMER");
                })
                .map(u -> String.valueOf(u.get("id")))
                .collect(Collectors.toList());
    }

    // Generic Fetcher Interface
    @FunctionalInterface
    interface ContentMapper {
        List<String> apply(List<Map<String, Object>> content);
    }

    private List<String> fetchIdsWithRetry(String serviceName, String baseUrl, String endpoint,
            ContentMapper customMapper) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("Fetching data from {} (attempt {}/{})...", serviceName, attempt, MAX_RETRIES);
                return fetchIdsWithPagination(serviceName, baseUrl, endpoint, customMapper);
            } catch (ResourceAccessException e) {
                log.warn("Could not connect to {} at {} (attempt {}/{}). Is it running?",
                        serviceName, baseUrl, attempt, MAX_RETRIES);
                handleRetry(attempt);
            } catch (Exception e) {
                log.error("Error fetching data from {}: {}", serviceName, e.getMessage(), e);
                handleRetry(attempt);
            }
        }
        log.error("FATAL: Failed to fetch data from {} after {} attempts", serviceName, MAX_RETRIES);
        throw new IllegalStateException(serviceName + " unavailable at " + baseUrl);
    }

    private void handleRetry(int attempt) {
        if (attempt < MAX_RETRIES) {
            try {
                log.info("Retrying in {}ms...", RETRY_DELAY_MS);
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("Retry interrupted");
            }
        }
    }

    private static final int MAX_PAGES = 50; // Safety break

    private List<String> fetchIdsWithPagination(String serviceName, String baseUrl, String endpoint,
            ContentMapper customMapper) {
        List<String> allIds = new ArrayList<>();
        int page = 0;
        boolean hasMore = true;

        while (hasMore && page < MAX_PAGES) {
            String url = java.util.Objects
                    .requireNonNull(String.format("%s%s?size=%d&page=%d", baseUrl, endpoint, PAGE_SIZE, page));

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, java.util.Objects.requireNonNull(HttpMethod.GET), null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("content")) {
                log.warn("Invalid response from {}: {}", serviceName, body);
                break;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) body.get("content");

            List<String> pageIds;
            if (customMapper != null) {
                pageIds = customMapper.apply(content);
            } else {
                // Default mapper: just extract "id"
                pageIds = content.stream()
                        .map(p -> String.valueOf(p.get("id"))) // Safe cast
                        .collect(Collectors.toList());
            }

            allIds.addAll(pageIds);

            Boolean isLast = (Boolean) body.get("last");
            hasMore = isLast == null || !isLast;
            page++;

            log.debug("Fetched page {} from {} with {} items (total so far: {})",
                    page, serviceName, pageIds.size(), allIds.size());
        }

        if (page >= MAX_PAGES) {
            log.warn("Hit MAX_PAGES limit ({}) while fetching from {}. Stopping.", MAX_PAGES, serviceName);
        }

        log.info("Fetched total of {} IDs from {}", allIds.size(), serviceName);
        return allIds;
    }
}
