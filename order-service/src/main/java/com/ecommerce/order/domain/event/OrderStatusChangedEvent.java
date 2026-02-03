package com.ecommerce.order.domain.event;

import com.ecommerce.common.domain.DomainEvent;
import com.ecommerce.order.domain.valueobject.OrderStatus;
import lombok.Getter;

import java.util.UUID;

@Getter
public class OrderStatusChangedEvent extends DomainEvent {

    private final UUID orderId;
    private final OrderStatus previousStatus;
    private final OrderStatus newStatus;

    public OrderStatusChangedEvent(UUID orderId, OrderStatus previousStatus, OrderStatus newStatus) {
        super();
        this.orderId = orderId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    @Override
    public String getEventType() {
        return "ORDER_STATUS_CHANGED";
    }
}
