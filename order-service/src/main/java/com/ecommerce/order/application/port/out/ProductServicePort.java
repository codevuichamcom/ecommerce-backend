package com.ecommerce.order.application.port.out;

import java.math.BigDecimal;

/**
 * Port for communicating with Product service.
 */
public interface ProductServicePort {

    /**
     * Get product details.
     */
    ProductDetails getProduct(String productId);

    /**
     * Product details.
     */
    record ProductDetails(
            String id,
            String name,
            BigDecimal price,
            String currency,
            boolean available) {
    }
}
