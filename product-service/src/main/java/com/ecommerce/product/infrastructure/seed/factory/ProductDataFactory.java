package com.ecommerce.product.infrastructure.seed.factory;

import com.ecommerce.product.domain.model.Money;
import com.ecommerce.product.domain.model.Product;

import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for generating realistic Product entities.
 */
@Component
@Profile("dev")
public class ProductDataFactory {

    private final Faker faker;

    public ProductDataFactory(Faker faker) {
        this.faker = faker;
    }

    public SeededData generateProducts() {
        List<Product> products = new ArrayList<>();
        int activeCount = 0;
        int inactiveCount = 0;
        int draftCount = 0;
        int discontinuedCount = 0;

        // Categories with their pricing logic
        // 1. Electronics (High price)
        for (int i = 0; i < 40; i++) {
            products.add(createProduct("Electronics", 50.0, 2000.0));
        }

        // 2. Books (Low price)
        for (int i = 0; i < 30; i++) {
            products.add(createProduct("Book", 10.0, 50.0));
        }

        // 3. Clothing (Medium price)
        for (int i = 0; i < 20; i++) {
            products.add(createProduct("Clothing", 20.0, 200.0));
        }

        // 4. Home (Medium price)
        for (int i = 0; i < 20; i++) {
            products.add(createProduct("Home", 30.0, 500.0));
        }

        // 5. Sports (Medium price)
        for (int i = 0; i < 10; i++) {
            products.add(createProduct("Sports", 15.0, 300.0));
        }

        // Apply state distribution
        // 80% ACTIVE, 10% INACTIVE, 5% DRAFT, 5% DISCONTINUED
        int total = products.size(); // should be 120
        for (int i = 0; i < total; i++) {
            Product p = products.get(i);

            if (i < total * 0.8) {
                p.activate();
                activeCount++;
            } else if (i < total * 0.9) {
                p.deactivate(); // Sets to INACTIVE
                inactiveCount++;
            } else if (i < total * 0.95) {
                // Keep as DRAFT
                draftCount++;
            } else {
                p.discontinue();
                discontinuedCount++;
            }
        }

        return new SeededData(products, activeCount, inactiveCount, draftCount, discontinuedCount);
    }

    private static final int MAX_NAME_LENGTH = 255;
    private static final int MIN_DESCRIPTION_LENGTH = 20;
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    private Product createProduct(String category, double minPrice, double maxPrice) {
        String name = generateProductName(category);
        String description = generateDescription();
        String sku = generateSku(category);
        Money price = generatePrice(minPrice, maxPrice);

        // Validate before creating
        validateProductData(name, description, sku, price);

        // Factory creates DRAFT by default
        return Product.create(name, description, sku, price);
    }

    private String generateProductName(String category) {
        String rawName;
        switch (category) {
            case "Electronics":
                rawName = faker.commerce().productName();
                break;
            case "Book":
                rawName = faker.book().title();
                break;
            case "Clothing":
                rawName = faker.commerce().productName() + " (" + faker.color().name() + ")";
                break;
            default:
                rawName = faker.commerce().productName();
        }

        // Truncate if too long and ensure not empty
        if (rawName.length() > MAX_NAME_LENGTH) {
            rawName = rawName.substring(0, MAX_NAME_LENGTH);
        }

        return rawName.trim();
    }

    private String generateDescription() {
        String desc = faker.lorem().sentence(10);

        // Ensure minimum length
        while (desc.length() < MIN_DESCRIPTION_LENGTH) {
            desc += " " + faker.lorem().sentence(5);
        }

        // Truncate if too long
        if (desc.length() > MAX_DESCRIPTION_LENGTH) {
            desc = desc.substring(0, MAX_DESCRIPTION_LENGTH);
        }

        return desc.trim();
    }

    private String generateSku(String category) {
        String prefix = category.substring(0, Math.min(3, category.length())).toUpperCase();
        return prefix + "-" + faker.number().digits(5) + "-" + faker.number().digits(3);
    }

    private Money generatePrice(double minPrice, double maxPrice) {
        double priceValue = faker.number().randomDouble(2, (int) minPrice, (int) maxPrice);
        // Ensure price is positive and has max 2 decimal places
        BigDecimal price = BigDecimal.valueOf(priceValue).setScale(2, java.math.RoundingMode.HALF_UP);
        return Money.of(price, "USD");
    }

    private void validateProductData(String name, String description, String sku, Money price) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }

        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Product name too long: " + name.length());
        }

        if (description == null || description.length() < MIN_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("Description too short: " +
                    (description == null ? 0 : description.length()));
        }

        if (sku == null || !sku.matches("[A-Z]{2,3}-\\d{5}-\\d{3}")) {
            throw new IllegalArgumentException("Invalid SKU format: " + sku);
        }

        if (price == null || price.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive: " + price);
        }
    }
}
