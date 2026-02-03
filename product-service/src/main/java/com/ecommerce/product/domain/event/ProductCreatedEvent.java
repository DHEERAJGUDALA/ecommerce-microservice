package com.ecommerce.product.domain.event;

import com.ecommerce.common.domain.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class ProductCreatedEvent extends DomainEvent {

    private final UUID productId;
    private final String name;
    private final String sku;
    private final BigDecimal price;
    private final Integer stockQuantity;

    public ProductCreatedEvent(UUID productId, String name, String sku, BigDecimal price, Integer stockQuantity) {
        super();
        this.productId = productId;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    @Override
    public String getEventType() {
        return "PRODUCT_CREATED";
    }
}
