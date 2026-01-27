package com.ecommerce.product.infrastructure.persistence.adapter;

import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.ProductId;
import com.ecommerce.product.domain.repository.ProductRepository;
import com.ecommerce.product.infrastructure.persistence.repository.ProductJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adapter implementing ProductRepository port.
 * Maps between domain entities and JPA entities.
 */
@Component
public class ProductPersistenceAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final com.ecommerce.product.infrastructure.persistence.mapper.ProductMapper productMapper;

    public ProductPersistenceAdapter(
            ProductJpaRepository jpaRepository,
            com.ecommerce.product.infrastructure.persistence.mapper.ProductMapper productMapper) {
        this.jpaRepository = jpaRepository;
        this.productMapper = productMapper;
    }

    @Override
    @SuppressWarnings("null")
    public Product save(Product product) {
        var entity = productMapper.toJpaEntity(product);
        var savedEntity = jpaRepository.save(entity);
        return productMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return jpaRepository.findById(Objects.requireNonNull(id.value()))
                .map(productMapper::toDomainEntity);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return jpaRepository.findBySku(sku)
                .map(productMapper::toDomainEntity);
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll().stream()
                .map(productMapper::toDomainEntity)
                .toList();
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }

    @Override
    public void delete(ProductId id) {
        jpaRepository.deleteById(Objects.requireNonNull(id.value()));
    }
}
