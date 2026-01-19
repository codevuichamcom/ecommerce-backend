# 🧪 Testing Guide

Complete testing strategy and guidelines for the E-commerce Backend.

---

## Table of Contents

1. [Testing Strategy](#testing-strategy)
2. [Test Types](#test-types)
3. [Testing Tools](#testing-tools)
4. [Writing Tests](#writing-tests)
5. [Running Tests](#running-tests)
6. [Test Coverage](#test-coverage)
7. [Testing Best Practices](#testing-best-practices)

---

## Testing Strategy

### Testing Pyramid

```
        ┌─────────────┐
        │   E2E Tests │  ← Few, slow, expensive
        │    (5%)     │
        ├─────────────┤
        │ Integration │  ← Some, medium speed
        │   Tests     │
        │    (25%)    │
        ├─────────────┤
        │ Unit Tests  │  ← Many, fast, cheap
        │    (70%)    │
        └─────────────┘
```

### Test Coverage Goals

| Test Type | Coverage Target | Execution Time |
|-----------|----------------|----------------|
| **Unit Tests** | 80% | < 5 minutes |
| **Integration Tests** | 60% | < 15 minutes |
| **E2E Tests** | Critical paths | < 30 minutes |

---

## Test Types

### 1. Unit Tests

**Purpose**: Test individual components in isolation

**Scope**: Single class/method

**Tools**: JUnit 5, Mockito, AssertJ

**Example**:
```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductService productService;
    
    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProduct() {
        // Given
        CreateProductRequest request = new CreateProductRequest(
            "Laptop Pro 15",
            "LAPTOP-PRO-15",
            new BigDecimal("1299.99"),
            "USD"
        );
        
        Product product = Product.create(
            request.name(),
            request.sku(),
            Money.of(request.price(), request.currency())
        );
        
        when(productRepository.save(any(Product.class)))
            .thenReturn(product);
        
        // When
        ProductResponse response = productService.createProduct(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Laptop Pro 15");
        assertThat(response.sku()).isEqualTo("LAPTOP-PRO-15");
        
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Should throw exception when SKU already exists")
    void shouldThrowExceptionWhenSkuExists() {
        // Given
        CreateProductRequest request = new CreateProductRequest(
            "Laptop Pro 15",
            "LAPTOP-PRO-15",
            new BigDecimal("1299.99"),
            "USD"
        );
        
        when(productRepository.existsBySku("LAPTOP-PRO-15"))
            .thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> productService.createProduct(request))
            .isInstanceOf(DuplicateSkuException.class)
            .hasMessage("Product with SKU LAPTOP-PRO-15 already exists");
    }
}
```

**What to Test**:
- ✅ Business logic
- ✅ Edge cases
- ✅ Error handling
- ✅ Validation rules

**What NOT to Test**:
- ❌ Framework code (Spring, JPA)
- ❌ Getters/Setters
- ❌ Trivial code

---

### 2. Integration Tests

**Purpose**: Test components working together

**Scope**: Multiple layers (Controller → Service → Repository)

**Tools**: Spring Boot Test, TestContainers, REST Assured

**Example**:
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("test_db")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ProductRepository productRepository;
    
    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Should create and retrieve product via REST API")
    void shouldCreateAndRetrieveProduct() {
        // Given
        CreateProductRequest request = new CreateProductRequest(
            "Laptop Pro 15",
            "LAPTOP-PRO-15",
            new BigDecimal("1299.99"),
            "USD"
        );
        
        // When - Create product
        ResponseEntity<ProductResponse> createResponse = restTemplate
            .postForEntity("/api/v1/products", request, ProductResponse.class);
        
        // Then - Verify creation
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        
        String productId = createResponse.getBody().id();
        
        // When - Retrieve product
        ResponseEntity<ProductResponse> getResponse = restTemplate
            .getForEntity("/api/v1/products/" + productId, ProductResponse.class);
        
        // Then - Verify retrieval
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().name()).isEqualTo("Laptop Pro 15");
        assertThat(getResponse.getBody().sku()).isEqualTo("LAPTOP-PRO-15");
    }
    
    @Test
    @DisplayName("Should return 409 when creating product with duplicate SKU")
    void shouldReturn409WhenDuplicateSku() {
        // Given - Create first product
        CreateProductRequest request = new CreateProductRequest(
            "Laptop Pro 15",
            "LAPTOP-PRO-15",
            new BigDecimal("1299.99"),
            "USD"
        );
        
        restTemplate.postForEntity("/api/v1/products", request, ProductResponse.class);
        
        // When - Try to create duplicate
        ResponseEntity<ProblemDetail> response = restTemplate
            .postForEntity("/api/v1/products", request, ProblemDetail.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getTitle()).contains("Duplicate SKU");
    }
}
```

**What to Test**:
- ✅ API endpoints (REST)
- ✅ Database operations
- ✅ Transaction boundaries
- ✅ Error responses

---

### 3. Kafka Integration Tests

**Purpose**: Test event publishing and consumption

**Tools**: Spring Kafka Test, TestContainers

**Example**:
```java
@SpringBootTest
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"order-events"})
class OrderEventIntegrationTest {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private OrderService orderService;
    
    @Test
    @DisplayName("Should publish OrderCreated event when order is created")
    void shouldPublishOrderCreatedEvent() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(
            "customer-123",
            List.of(new OrderItemRequest("product-1", 2))
        );
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> receivedEvent = new AtomicReference<>();
        
        // Subscribe to topic
        kafkaTemplate.setConsumerFactory(consumerFactory);
        Consumer<String, String> consumer = consumerFactory.createConsumer();
        consumer.subscribe(Collections.singletonList("order-events"));
        
        // When
        OrderResponse order = orderService.createOrder(request);
        
        // Then - Wait for event
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
        assertThat(records).isNotEmpty();
        
        String eventJson = records.iterator().next().value();
        assertThat(eventJson).contains("OrderCreated");
        assertThat(eventJson).contains(order.id());
    }
}
```

---

### 4. Contract Tests

**Purpose**: Verify API contracts between services

**Tools**: Spring Cloud Contract, Pact

**Example** (Provider):
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureStubRunner(
    ids = "com.ecommerce:product-service:+:stubs:8081",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class ProductContractTest {
    
    @Test
    void shouldReturnProductById() {
        // Contract defined in product-service
        // Consumer (order-service) expects this response
        
        given()
            .when()
            .get("/api/v1/products/01HQZX3Y4Z5A6B7C8D9E0F1G2H")
            .then()
            .statusCode(200)
            .body("id", equalTo("01HQZX3Y4Z5A6B7C8D9E0F1G2H"))
            .body("name", notNullValue())
            .body("price", greaterThan(0));
    }
}
```

---

### 5. End-to-End Tests

**Purpose**: Test complete user workflows

**Scope**: Multiple services, full stack

**Tools**: REST Assured, TestContainers

**Example**:
```java
@SpringBootTest
@Testcontainers
class OrderE2ETest {
    
    @Test
    @DisplayName("Complete order flow: Create → Reserve Stock → Payment → Confirm")
    void shouldCompleteOrderFlow() {
        // 1. Create product
        String productId = createProduct("Laptop Pro 15", "LAPTOP-PRO-15", 1299.99);
        
        // 2. Add inventory
        addInventory(productId, 100);
        
        // 3. Create order
        String orderId = createOrder("customer-123", productId, 2);
        
        // 4. Wait for saga completion
        await().atMost(10, SECONDS)
            .until(() -> getOrderStatus(orderId), equalTo("CONFIRMED"));
        
        // 5. Verify inventory reduced
        int remainingStock = getInventory(productId);
        assertThat(remainingStock).isEqualTo(98);
        
        // 6. Verify payment created
        Payment payment = getPaymentForOrder(orderId);
        assertThat(payment.status()).isEqualTo("COMPLETED");
    }
}
```

---

## Testing Tools

### Core Testing Framework

**JUnit 5**:
```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

### Mocking

**Mockito**:
```java
@Mock
private ProductRepository productRepository;

@InjectMocks
private ProductService productService;

when(productRepository.findById("123")).thenReturn(Optional.of(product));
verify(productRepository).save(any(Product.class));
```

### Assertions

**AssertJ**:
```java
assertThat(product.getName()).isEqualTo("Laptop Pro 15");
assertThat(product.getPrice()).isGreaterThan(BigDecimal.ZERO);
assertThat(products).hasSize(10).extracting("name").contains("Laptop");
```

### TestContainers

**PostgreSQL**:
```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
```

**Kafka**:
```java
@Container
static KafkaContainer kafka = new KafkaContainer(
    DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
);
```

### REST Testing

**REST Assured**:
```java
given()
    .contentType(ContentType.JSON)
    .body(request)
.when()
    .post("/api/v1/products")
.then()
    .statusCode(201)
    .body("name", equalTo("Laptop Pro 15"));
```

---

## Writing Tests

### Test Structure (Given-When-Then)

```java
@Test
void shouldCalculateOrderTotal() {
    // Given - Setup test data
    Order order = new Order();
    order.addItem(new OrderItem("product-1", 2, new BigDecimal("100")));
    order.addItem(new OrderItem("product-2", 1, new BigDecimal("50")));
    
    // When - Execute the action
    BigDecimal total = order.calculateTotal();
    
    // Then - Verify the result
    assertThat(total).isEqualByComparingTo(new BigDecimal("250"));
}
```

### Test Naming Conventions

**Pattern**: `should[ExpectedBehavior]When[Condition]`

**Examples**:
```java
shouldCreateProductWhenValidRequest()
shouldThrowExceptionWhenSkuAlreadyExists()
shouldReturnEmptyListWhenNoProductsExist()
shouldReserveStockWhenSufficientQuantityAvailable()
```

### Test Data Builders

```java
public class ProductTestBuilder {
    private String name = "Test Product";
    private String sku = "TEST-SKU";
    private BigDecimal price = new BigDecimal("99.99");
    private String currency = "USD";
    
    public ProductTestBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public ProductTestBuilder withSku(String sku) {
        this.sku = sku;
        return this;
    }
    
    public Product build() {
        return Product.create(name, sku, Money.of(price, currency));
    }
}

// Usage
Product product = new ProductTestBuilder()
    .withName("Laptop Pro 15")
    .withSku("LAPTOP-PRO-15")
    .build();
```

---

## Running Tests

### Gradle Commands

```bash
# Run all tests
./gradlew test

# Run tests for specific service
./gradlew :product-service:test

# Run integration tests
./gradlew integrationTest

# Run specific test class
./gradlew test --tests ProductServiceTest

# Run specific test method
./gradlew test --tests ProductServiceTest.shouldCreateProduct

# Run tests with coverage
./gradlew test jacocoTestReport

# Run tests in parallel
./gradlew test --parallel --max-workers=4
```

### IDE Integration

**IntelliJ IDEA**:
- Right-click test class → Run
- Ctrl+Shift+F10 (Windows/Linux) or Cmd+Shift+R (Mac)
- View coverage: Run → Run with Coverage

**VS Code**:
- Install Java Test Runner extension
- Click "Run Test" above test method

---

## Test Coverage

### JaCoCo Configuration

```gradle
// build.gradle.kts
plugins {
    jacoco
}

jacoco {
    toolVersion = "0.8.10"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
```

### Coverage Goals

| Layer | Minimum Coverage |
|-------|------------------|
| **Domain Layer** | 90% |
| **Application Layer** | 80% |
| **Infrastructure Layer** | 60% |
| **Overall** | 80% |

### Viewing Coverage

```bash
# Generate report
./gradlew test jacocoTestReport

# Open report
open build/reports/jacoco/test/html/index.html
```

---

## Testing Best Practices

### ✅ Do

**Write Tests First** (TDD):
```java
// 1. Write failing test
@Test
void shouldCalculateDiscount() {
    BigDecimal discount = order.calculateDiscount();
    assertThat(discount).isEqualByComparingTo(new BigDecimal("10"));
}

// 2. Implement feature
// 3. Test passes
```

**Test One Thing**:
```java
// ✅ Good - Tests one behavior
@Test
void shouldReturnTrueWhenStockAvailable() {
    assertThat(inventory.hasStock(10)).isTrue();
}

// ❌ Bad - Tests multiple things
@Test
void shouldHandleInventory() {
    inventory.reserve(10);
    assertThat(inventory.getAvailable()).isEqualTo(90);
    inventory.release(5);
    assertThat(inventory.getAvailable()).isEqualTo(95);
}
```

**Use Descriptive Names**:
```java
// ✅ Good
@Test
void shouldThrowInsufficientStockExceptionWhenQuantityExceedsAvailable()

// ❌ Bad
@Test
void test1()
```

**Isolate Tests**:
```java
@BeforeEach
void setUp() {
    productRepository.deleteAll();
}
```

**Test Edge Cases**:
```java
@Test
void shouldHandleZeroQuantity()

@Test
void shouldHandleNegativePrice()

@Test
void shouldHandleNullInput()

@Test
void shouldHandleVeryLargeNumbers()
```

### ❌ Don't

**Don't Test Implementation Details**:
```java
// ❌ Bad - Tests internal state
@Test
void shouldSetInternalFlag() {
    product.activate();
    assertThat(product.isActiveFlag).isTrue();
}

// ✅ Good - Tests behavior
@Test
void shouldBeActiveAfterActivation() {
    product.activate();
    assertThat(product.isActive()).isTrue();
}
```

**Don't Use Random Data**:
```java
// ❌ Bad - Non-deterministic
@Test
void shouldCreateProduct() {
    String name = UUID.randomUUID().toString();
    // Test may fail randomly
}

// ✅ Good - Deterministic
@Test
void shouldCreateProduct() {
    String name = "Test Product";
    // Test always behaves the same
}
```

**Don't Ignore Flaky Tests**:
```java
// ❌ Bad
@Test
@Disabled("Flaky test, fix later")
void shouldProcessOrder() {
    // ...
}

// ✅ Good - Fix the test
@Test
void shouldProcessOrder() {
    // Use proper synchronization, mocking, etc.
}
```

---

## Continuous Integration

### GitHub Actions

```yaml
# .github/workflows/test.yml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      
      - name: Run Unit Tests
        run: ./gradlew test
      
      - name: Run Integration Tests
        run: ./gradlew integrationTest
      
      - name: Generate Coverage Report
        run: ./gradlew jacocoTestReport
      
      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./build/reports/jacoco/test/jacocoTestReport.xml
```

---

## Next Steps

- [Development Guide](DEVELOPMENT.md) - Local testing setup
- [CI/CD Guide](DEPLOYMENT.md) - Automated testing
- [Troubleshooting](TROUBLESHOOTING.md) - Test failures

---

**Document Version**: 1.0  
**Last Updated**: 2026-01-19  
**Maintained By**: QA Team
