package com.ecommerce.product.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductDto(
        UUID id,
        String name,
        String description,
        String sku,
        BigDecimal price,
        String currency,
        Integer stockQuantity,
        UUID categoryId,
        String status,
        boolean available,
        Instant createdAt,
        Instant updatedAt
) {}
