package com.ecommerce.product.infrastructure.persistence.adapter;

import com.ecommerce.product.domain.model.Money;
import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.ProductId;
import com.ecommerce.product.domain.repository.ProductRepository;
import com.ecommerce.product.infrastructure.persistence.entity.ProductJpaEntity;
import com.ecommerce.product.infrastructure.persistence.repository.ProductJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing ProductRepository port.
 * Maps between domain entities and JPA entities.
 */
@Component
public class ProductPersistenceAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    public ProductPersistenceAdapter(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        var entity = toJpaEntity(product);
        var savedEntity = jpaRepository.save(entity);
        return toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return jpaRepository.findById(id.value())
                .map(this::toDomainEntity);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return jpaRepository.findBySku(sku)
                .map(this::toDomainEntity);
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomainEntity)
                .toList();
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }

    @Override
    public void delete(ProductId id) {
        jpaRepository.deleteById(id.value());
    }

    // Mapping methods

    private ProductJpaEntity toJpaEntity(Product product) {
        var entity = new ProductJpaEntity();
        entity.setId(product.getId().value());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setSku(product.getSku());
        entity.setPrice(product.getPrice().amount());
        entity.setCurrency(product.getPrice().currency().getCurrencyCode());
        entity.setStatus(product.getStatus());
        entity.setCreatedAt(product.getCreatedAt());
        entity.setUpdatedAt(product.getUpdatedAt());
        return entity;
    }

    private Product toDomainEntity(ProductJpaEntity entity) {
        return Product.reconstitute(
                new ProductId(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                entity.getSku(),
                new Money(entity.getPrice(), Currency.getInstance(entity.getCurrency())),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
