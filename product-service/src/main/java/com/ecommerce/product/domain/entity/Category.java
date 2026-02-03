package com.ecommerce.product.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    private UUID id;
    private String name;
    private String description;
    private UUID parentId;
    private Instant createdAt;
    private Instant updatedAt;

    private Category(String name, String description, UUID parentId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.parentId = parentId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Category create(String name, String description) {
        return new Category(name, description, null);
    }

    public static Category createWithParent(String name, String description, UUID parentId) {
        return new Category(name, description, parentId);
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public boolean isRootCategory() {
        return parentId == null;
    }
}
