package com.ecommerce.product.application.service;

import com.ecommerce.common.exception.ConflictException;
import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.product.application.dto.CreateProductCommand;
import com.ecommerce.product.application.dto.ProductResponse;
import com.ecommerce.product.application.dto.UpdateProductCommand;
import com.ecommerce.product.domain.model.Money;
import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.ProductId;
import com.ecommerce.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Product application service.
 * Orchestrates use cases and manages transactions.
 */
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Create a new product.
     */
    public ProductResponse createProduct(CreateProductCommand command) {
        // Check for duplicate SKU
        if (productRepository.existsBySku(command.sku())) {
            throw ConflictException.duplicate("Product", command.sku());
        }

        // Create domain entity
        var product = Product.create(
                command.name(),
                command.description(),
                command.sku(),
                Money.of(command.price()));

        // Persist and return
        var savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }

    /**
     * Get product by ID.
     */
    @Transactional(readOnly = true)
    public ProductResponse getProduct(String id) {
        var product = findProductOrThrow(id);
        return ProductResponse.from(product);
    }

    /**
     * Get all products.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    /**
     * Update product details.
     */
    public ProductResponse updateProduct(String id, UpdateProductCommand command) {
        var product = findProductOrThrow(id);

        Money newPrice = command.price() != null ? Money.of(command.price()) : null;
        product.updateDetails(command.name(), command.description(), newPrice);

        var savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }

    /**
     * Activate a product.
     */
    public ProductResponse activateProduct(String id) {
        var product = findProductOrThrow(id);
        product.activate();
        var savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }

    /**
     * Deactivate a product.
     */
    public ProductResponse deactivateProduct(String id) {
        var product = findProductOrThrow(id);
        product.deactivate();
        var savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }

    /**
     * Discontinue a product.
     */
    public ProductResponse discontinueProduct(String id) {
        var product = findProductOrThrow(id);
        product.discontinue();
        var savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }

    /**
     * Delete a product.
     */
    public void deleteProduct(String id) {
        var productId = new ProductId(id);
        if (productRepository.findById(productId).isEmpty()) {
            throw new NotFoundException("Product", id);
        }
        productRepository.delete(productId);
    }

    private Product findProductOrThrow(String id) {
        var productId = new ProductId(id);
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product", id));
    }
}
