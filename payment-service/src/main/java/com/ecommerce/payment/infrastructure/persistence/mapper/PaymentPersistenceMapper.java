package com.ecommerce.payment.infrastructure.persistence.mapper;

import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentPersistenceMapper {

    public PaymentJpaEntity toJpaEntity(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentJpaEntity.builder()
            .id(payment.getId())
            .orderId(payment.getOrderId())
            .customerId(payment.getCustomerId())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .method(payment.getMethod())
            .status(payment.getStatus())
            .transactionId(payment.getTransactionId())
            .failureReason(payment.getFailureReason())
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .completedAt(payment.getCompletedAt())
            .build();
    }

    public Payment toDomain(PaymentJpaEntity entity) {
        // Cannot reconstruct domain entity with protected constructor
        throw new UnsupportedOperationException("Conversion from JPA to domain not supported");
    }
}
