package com.ecommerce.product.infrastructure.persistence.repository;

import com.ecommerce.product.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Product.
 */
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, String> {

    Optional<ProductJpaEntity> findBySku(String sku);

    boolean existsBySku(String sku);
}
