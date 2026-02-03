package com.ecommerce.payment.domain.entity;

import com.ecommerce.payment.domain.valueobject.PaymentMethod;
import com.ecommerce.payment.domain.valueobject.PaymentStatus;
import com.ecommerce.common.exception.BusinessException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    private UUID id;
    private UUID orderId;
    private UUID customerId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionId;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    private Payment(UUID orderId, UUID customerId, BigDecimal amount, String currency, PaymentMethod method) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.method = method;
        this.status = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Payment create(UUID orderId, UUID customerId, BigDecimal amount, String currency, PaymentMethod method) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        return new Payment(orderId, customerId, amount, currency, method);
    }

    public void process(String transactionId) {
        if (status != PaymentStatus.PENDING) {
            throw new BusinessException("INVALID_STATUS", "Payment can only be processed from PENDING status");
        }
        this.transactionId = transactionId;
        this.status = PaymentStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void complete() {
        if (status != PaymentStatus.PROCESSING) {
            throw new BusinessException("INVALID_STATUS", "Payment can only be completed from PROCESSING status");
        }
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void fail(String reason) {
        if (status == PaymentStatus.COMPLETED || status == PaymentStatus.REFUNDED) {
            throw new BusinessException("INVALID_STATUS", "Cannot fail completed or refunded payment");
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    public void refund() {
        if (status != PaymentStatus.COMPLETED) {
            throw new BusinessException("INVALID_STATUS", "Only completed payments can be refunded");
        }
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = Instant.now();
    }

    public boolean isSuccessful() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean canBeRefunded() {
        return status == PaymentStatus.COMPLETED;
    }
}
