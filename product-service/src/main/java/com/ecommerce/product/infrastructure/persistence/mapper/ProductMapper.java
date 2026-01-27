package com.ecommerce.product.infrastructure.persistence.mapper;

import com.ecommerce.product.domain.model.Money;
import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.model.ProductId;
import com.ecommerce.product.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

/**
 * Mapper between Product domain model and JPA entities.
 */
@Component
public class ProductMapper {

    public ProductJpaEntity toJpaEntity(Product product) {
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

    public Product toDomainEntity(ProductJpaEntity entity) {
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
