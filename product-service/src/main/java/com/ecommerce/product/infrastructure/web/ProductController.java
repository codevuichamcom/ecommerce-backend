package com.ecommerce.product.infrastructure.web;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.product.application.dto.CreateProductCommand;
import com.ecommerce.product.application.dto.ProductResponse;
import com.ecommerce.product.application.dto.UpdateProductCommand;
import com.ecommerce.product.application.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Product operations.
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductCommand command) {
        var product = productService.createProduct(command);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(product));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable String id) {
        var product = productService.getProduct(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping
    @Operation(summary = "Get all products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        var products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product details")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductCommand command) {
        var product = productService.updateProduct(id, command);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate a product")
    public ResponseEntity<ApiResponse<ProductResponse>> activateProduct(@PathVariable String id) {
        var product = productService.activateProduct(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a product")
    public ResponseEntity<ApiResponse<ProductResponse>> deactivateProduct(@PathVariable String id) {
        var product = productService.deactivateProduct(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping("/{id}/discontinue")
    @Operation(summary = "Discontinue a product")
    public ResponseEntity<ApiResponse<ProductResponse>> discontinueProduct(@PathVariable String id) {
        var product = productService.discontinueProduct(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
    }
}
