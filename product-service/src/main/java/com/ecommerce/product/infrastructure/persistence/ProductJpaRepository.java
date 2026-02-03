package com.ecommerce.product.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

    Optional<ProductJpaEntity> findBySku(String sku);

    List<ProductJpaEntity> findByIdIn(List<UUID> ids);

    Page<ProductJpaEntity> findByCategoryId(UUID categoryId, Pageable pageable);

    boolean existsBySku(String sku);
}
