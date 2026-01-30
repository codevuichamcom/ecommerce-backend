package com.ecommerce.inventory.infrastructure.persistence.repository;

import com.ecommerce.inventory.infrastructure.persistence.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, String> {
}
