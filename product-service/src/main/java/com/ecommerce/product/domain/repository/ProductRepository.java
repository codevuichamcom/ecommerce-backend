package com.ecommerce.product.domain.repository;

import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.ProductId;

import java.util.List;
import java.util.Optional;

/**
 * Product repository interface (port).
 * Domain layer defines what it needs - infrastructure implements.
 */
public interface ProductRepository {

    /**
     * Save a product.
     */
    Product save(Product product);

    /**
     * Find product by ID.
     */
    Optional<Product> findById(ProductId id);

    /**
     * Find product by SKU.
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find all products.
     */
    List<Product> findAll();

    /**
     * Check if SKU exists.
     */
    boolean existsBySku(String sku);

    /**
     * Delete a product.
     */
    void delete(ProductId id);
}
