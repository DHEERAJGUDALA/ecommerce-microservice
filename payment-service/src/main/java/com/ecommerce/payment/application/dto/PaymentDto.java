package com.ecommerce.payment.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentDto(
        UUID id,
        UUID orderId,
        UUID customerId,
        BigDecimal amount,
        String currency,
        String method,
        String status,
        String transactionId,
        String failureReason,
        Instant createdAt,
        Instant completedAt
) {}
