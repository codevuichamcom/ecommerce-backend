package com.ecommerce.product.application.service;

import com.ecommerce.common.exception.ConflictException;
import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.product.application.dto.CreateProductCommand;
import com.ecommerce.product.application.dto.ProductResponse;
import com.ecommerce.product.application.dto.UpdateProductCommand;
import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.ProductId;
import com.ecommerce.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private CreateProductCommand createCommand;
    private Product product;
    private String productId = "prod_123";

    @BeforeEach
    void setUp() {
        createCommand = new CreateProductCommand(
                "Test Product",
                "Description",
                "SKU-123",
                new BigDecimal("99.99"));

        product = Product.create(
                createCommand.name(),
                createCommand.description(),
                createCommand.sku(),
                com.ecommerce.product.domain.model.Money.of(createCommand.price()));
        // Manually set ID if needed, but Product.create usually uses ULID if
        // implemented that way.
        // Let's assume the repository save will return the product with an ID.
    }

    @Test
    void createProduct_ShouldSaveAndReturnProduct() {
        // Given
        when(productRepository.existsBySku(createCommand.sku())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.createProduct(createCommand);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.sku()).isEqualTo(createCommand.sku());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_ShouldThrowConflictException_WhenSkuExists() {
        // Given
        when(productRepository.existsBySku(createCommand.sku())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(createCommand))
                .isInstanceOf(ConflictException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void getProduct_ShouldReturnProduct_WhenExists() {
        // Given
        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(product));

        // When
        ProductResponse response = productService.getProduct(productId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.sku()).isEqualTo(product.getSku());
    }

    @Test
    void getProduct_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProduct(productId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllProducts_ShouldReturnList() {
        // Given
        when(productRepository.findAll()).thenReturn(List.of(product));

        // When
        List<ProductResponse> responses = productService.getAllProducts();

        // Then
        assertThat(responses).hasSize(1);
    }

    @Test
    void updateProduct_ShouldUpdateAndSave() {
        // Given
        UpdateProductCommand updateCommand = new UpdateProductCommand(
                "Updated Name",
                "Updated Description",
                new BigDecimal("149.99"));
        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.updateProduct(productId, updateCommand);

        // Then
        assertThat(response).isNotNull();
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_ShouldDelete_WhenExists() {
        // Given
        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.of(product));

        // When
        productService.deleteProduct(productId);

        // Then
        verify(productRepository).delete(any(ProductId.class));
    }

    @Test
    void deleteProduct_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(NotFoundException.class);
    }
}
