package com.ecommerce.payment.domain.event;

import com.ecommerce.common.domain.DomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class PaymentFailedEvent extends DomainEvent {

    private final UUID paymentId;
    private final UUID orderId;
    private final String reason;

    public PaymentFailedEvent(UUID paymentId, UUID orderId, String reason) {
        super();
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.reason = reason;
    }

    @Override
    public String getEventType() {
        return "PAYMENT_FAILED";
    }
}
