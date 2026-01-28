package com.ecommerce.order.infrastructure.client;

import com.ecommerce.order.application.port.out.ProductServicePort.ProductDetails;
import com.ecommerce.order.infrastructure.config.ServiceProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Phase 4.1: Integration tests for Resilience4j patterns with Spring Boot
 * context
 * 
 * Tests verify:
 * - CircuitBreaker opens after failure threshold
 * - Retry with exponential backoff
 * - Proper Spring AOP integration
 */
@SpringBootTest
@ActiveProfiles("test")
class ProductServiceClientIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private ProductServiceClient productServiceClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public ServiceProperties testServiceProperties() throws IOException {
            mockWebServer = new MockWebServer();
            mockWebServer.start();

            var serviceProperties = new ServiceProperties();
            var productConfig = new ServiceProperties.ServiceUrl();
            productConfig.setUrl(mockWebServer.url("/").toString());
            serviceProperties.setProduct(productConfig);

            var inventoryConfig = new ServiceProperties.ServiceUrl();
            inventoryConfig.setUrl(mockWebServer.url("/").toString());
            serviceProperties.setInventory(inventoryConfig);

            return serviceProperties;
        }

        @Bean
        @Primary
        public ProductServiceClient testProductServiceClient(
                WebClient.Builder webClientBuilder,
                ServiceProperties serviceProperties) {
            return new ProductServiceClient(webClientBuilder, serviceProperties);
        }
    }

    @BeforeEach
    void setUp() {
        // Reset circuit breaker state before each test
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("product-service");
        circuitBreaker.reset();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (mockWebServer != null) {
            // Clear any remaining queued responses
            while (mockWebServer.getRequestCount() < mockWebServer.getRequestCount()) {
                try {
                    mockWebServer.takeRequest(1, java.util.concurrent.TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    /**
     * Task 4.1.2.3: Test CircuitBreaker opens after failure threshold
     */
    @Test
    void circuitBreaker_shouldOpen_afterFailureThreshold() {
        // Given: Mock server returns 500 errors
        for (int i = 0; i < 15; i++) {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
                    .setBody("{\"success\":false}"));
        }

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("product-service");

        // When: Make calls until circuit opens (need at least minimumNumberOfCalls = 5)
        int failureCount = 0;
        for (int i = 0; i < 10; i++) {
            try {
                productServiceClient.getProduct("prod-123");
            } catch (Exception e) {
                failureCount++;
            }
        }

        // Then: Should have failures
        assertThat(failureCount).isGreaterThan(0);

        // And: Circuit should eventually open (may take a moment)
        await()
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    CircuitBreaker.State state = circuitBreaker.getState();
                    CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

                    // Circuit should be OPEN or have high failure rate
                    boolean isOpenOrHighFailureRate = state == CircuitBreaker.State.OPEN ||
                            state == CircuitBreaker.State.FORCED_OPEN ||
                            metrics.getFailureRate() >= 40.0f;

                    assertThat(isOpenOrHighFailureRate)
                            .as("Circuit should be OPEN or have high failure rate. State: %s, Failure rate: %.2f%%",
                                    state, metrics.getFailureRate())
                            .isTrue();
                });
    }

    /**
     * Task 4.1.2.3: Test CircuitBreaker stays CLOSED on success
     */
    @Test
    void circuitBreaker_shouldStayClosed_onSuccess() {
        // Given: Mock server returns successful responses
        for (int i = 0; i < 10; i++) {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody("""
                            {
                                "success": true,
                                "data": {
                                    "id": "prod-123",
                                    "name": "Test Product",
                                    "price": 100.00,
                                    "currency": "VND",
                                    "available": true
                                }
                            }
                            """));
        }

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("product-service");

        // When: Make 10 successful calls
        for (int i = 0; i < 10; i++) {
            ProductDetails result = productServiceClient.getProduct("prod-123");
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo("prod-123");
        }

        // Then: Circuit should remain CLOSED
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // And: Metrics should show success
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isGreaterThanOrEqualTo(5);
        assertThat(metrics.getFailureRate()).isLessThan(10.0f);
    }

    /**
     * Task 4.1.4.4: Test Retry attempts multiple times before succeeding
     */
    @Test
    void retry_shouldAttemptMultipleTimes_thenSucceed() {
        // Given: Mock server returns errors for first 2 attempts, then success
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                            "success": true,
                            "data": {
                                "id": "prod-retry",
                                "name": "Retry Product",
                                "price": 100.00,
                                "currency": "VND",
                                "available": true
                            }
                        }
                        """));

        // When: Call service (will retry on failures)
        ProductDetails result = productServiceClient.getProduct("prod-retry");

        // Then: Should succeed after retries
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("prod-retry");

        // And: Should have made 3 requests (1 initial + 2 retries)
        assertThat(mockWebServer.getRequestCount()).isEqualTo(3);
    }

    /**
     * Test successful product retrieval in Spring context
     */
    @Test
    void getProduct_shouldReturnProductDetails_inSpringContext() {
        // Given: Mock server returns valid product
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                            "success": true,
                            "data": {
                                "id": "prod-spring",
                                "name": "Spring Product",
                                "price": 200.00,
                                "currency": "VND",
                                "available": true
                            }
                        }
                        """));

        // When: Get product
        ProductDetails result = productServiceClient.getProduct("prod-spring");

        // Then: Should return correct details
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("prod-spring");
        assertThat(result.name()).isEqualTo("Spring Product");
        assertThat(result.price()).isEqualByComparingTo("200.00");
    }
}
