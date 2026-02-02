package com.ecommerce.product.infrastructure.seed.factory;

import com.ecommerce.product.domain.model.Product;

import java.util.List;

/**
 * Container for seeded data returned by the factory.
 */
public record SeededData(
        List<Product> products,
        int activeCount,
        int inactiveCount,
        int draftCount,
        int discontinuedCount) {
    public int totalCount() {
        return products.size();
    }
}
