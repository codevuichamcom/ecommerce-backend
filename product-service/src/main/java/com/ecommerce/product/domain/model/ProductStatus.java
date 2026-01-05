package com.ecommerce.product.domain.model;

/**
 * Product status enum.
 */
public enum ProductStatus {
    DRAFT, // Product being created, not visible
    ACTIVE, // Product available for sale
    INACTIVE, // Temporarily unavailable
    DISCONTINUED // No longer sold
}
