package com.ecommerce.product.domain.repository;

import com.ecommerce.product.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID id);

    Optional<Product> findBySku(String sku);

    List<Product> findByIds(List<UUID> ids);

    Page<Product> findAll(Pageable pageable);

    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);

    void delete(Product product);

    boolean existsBySku(String sku);
}
