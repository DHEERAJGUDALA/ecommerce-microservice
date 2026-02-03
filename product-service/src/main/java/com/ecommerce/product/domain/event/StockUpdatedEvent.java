package com.ecommerce.product.domain.event;

import com.ecommerce.common.domain.DomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class StockUpdatedEvent extends DomainEvent {

    private final UUID productId;
    private final Integer previousQuantity;
    private final Integer newQuantity;

    public StockUpdatedEvent(UUID productId, Integer previousQuantity, Integer newQuantity) {
        super();
        this.productId = productId;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
    }

    @Override
    public String getEventType() {
        return "STOCK_UPDATED";
    }
}
