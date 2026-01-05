package com.ecommerce.product.domain.model;

import com.ecommerce.common.domain.AggregateRoot;
import com.ecommerce.common.util.IdGenerator;

import java.time.Instant;
import java.util.Objects;

/**
 * Product aggregate root.
 * Represents a product in the catalog.
 */
public class Product extends AggregateRoot<ProductId> {

    private final ProductId id;
    private String name;
    private String description;
    private String sku;
    private Money price;
    private ProductStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    // Private constructor - use factory methods
    private Product(ProductId id, String name, String description, String sku,
            Money price, ProductStatus status, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     * Create a new product.
     */
    public static Product create(String name, String description, String sku, Money price) {
        Objects.requireNonNull(name, "Name must not be null");
        Objects.requireNonNull(sku, "SKU must not be null");
        Objects.requireNonNull(price, "Price must not be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        if (sku.isBlank()) {
            throw new IllegalArgumentException("SKU must not be blank");
        }

        var productId = new ProductId(IdGenerator.generate("prd"));
        return new Product(
                productId,
                name.trim(),
                description != null ? description.trim() : "",
                sku.trim().toUpperCase(),
                price,
                ProductStatus.DRAFT,
                Instant.now());
    }

    /**
     * Reconstitute from persistence.
     */
    public static Product reconstitute(ProductId id, String name, String description,
            String sku, Money price, ProductStatus status,
            Instant createdAt, Instant updatedAt) {
        var product = new Product(id, name, description, sku, price, status, createdAt);
        product.updatedAt = updatedAt;
        return product;
    }

    /**
     * Activate the product (make it available for sale).
     */
    public void activate() {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new IllegalStateException("Cannot activate a discontinued product");
        }
        this.status = ProductStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Deactivate the product (temporarily unavailable).
     */
    public void deactivate() {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new IllegalStateException("Cannot deactivate a discontinued product");
        }
        this.status = ProductStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Discontinue the product (permanently remove from sale).
     */
    public void discontinue() {
        this.status = ProductStatus.DISCONTINUED;
        this.updatedAt = Instant.now();
    }

    /**
     * Update product details.
     */
    public void updateDetails(String name, String description, Money price) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description.trim();
        }
        if (price != null) {
            this.price = price;
        }
        this.updatedAt = Instant.now();
    }

    // Getters
    @Override
    public ProductId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSku() {
        return sku;
    }

    public Money getPrice() {
        return price;
    }

    public ProductStatus getStatus() {
        return status;
    }

    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE;
    }
}
