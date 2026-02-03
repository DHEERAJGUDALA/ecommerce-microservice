package com.ecommerce.payment.domain.event;

import com.ecommerce.common.domain.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class PaymentCompletedEvent extends DomainEvent {

    private final UUID paymentId;
    private final UUID orderId;
    private final BigDecimal amount;
    private final String transactionId;

    public PaymentCompletedEvent(UUID paymentId, UUID orderId, BigDecimal amount, String transactionId) {
        super();
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.transactionId = transactionId;
    }

    @Override
    public String getEventType() {
        return "PAYMENT_COMPLETED";
    }
}
