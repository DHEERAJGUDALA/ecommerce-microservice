package com.ecommerce.order.domain.event;

import com.ecommerce.common.domain.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class OrderCreatedEvent extends DomainEvent {

    private final UUID orderId;
    private final UUID customerId;
    private final BigDecimal totalAmount;

    public OrderCreatedEvent(UUID orderId, UUID customerId, BigDecimal totalAmount) {
        super();
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
    }

    @Override
    public String getEventType() {
        return "ORDER_CREATED";
    }
}
