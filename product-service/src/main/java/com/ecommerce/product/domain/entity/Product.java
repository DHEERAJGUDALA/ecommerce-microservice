package com.ecommerce.product.domain.entity;

import com.ecommerce.product.domain.valueobject.Money;
import com.ecommerce.product.domain.valueobject.ProductStatus;
import com.ecommerce.common.exception.BusinessException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    private UUID id;
    private String name;
    private String description;
    private String sku;
    private Money price;
    private Integer stockQuantity;
    private UUID categoryId;
    private ProductStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private Product(String name, String description, String sku, Money price, Integer stockQuantity, UUID categoryId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.categoryId = categoryId;
        this.status = ProductStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Product create(String name, String description, String sku, Money price, Integer stockQuantity, UUID categoryId) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        return new Product(name, description, sku, price, stockQuantity, categoryId);
    }

    public void updateDetails(String name, String description, Money price) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.updatedAt = Instant.now();
    }

    public void updateStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        this.stockQuantity = quantity;
        this.updatedAt = Instant.now();
    }

    public void reserveStock(int quantity) {
        if (quantity > stockQuantity) {
            throw new BusinessException("INSUFFICIENT_STOCK",
                    String.format("Insufficient stock. Available: %d, Requested: %d", stockQuantity, quantity));
        }
        this.stockQuantity -= quantity;
        this.updatedAt = Instant.now();
    }

    public void releaseStock(int quantity) {
        this.stockQuantity += quantity;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = ProductStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE && stockQuantity > 0;
    }

    public boolean hasStock(int quantity) {
        return stockQuantity >= quantity;
    }
}
