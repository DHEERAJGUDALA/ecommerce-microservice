package com.ecommerce.product.domain.repository;

import com.ecommerce.product.domain.entity.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(UUID id);

    List<Category> findAll();

    List<Category> findByParentId(UUID parentId);

    void delete(Category category);
}
