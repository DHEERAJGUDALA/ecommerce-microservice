package com.ecommerce.payment.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentEventPayload(
    @JsonProperty("paymentId") UUID paymentId,
    @JsonProperty("orderId") UUID orderId,
    @JsonProperty("customerId") UUID customerId,
    @JsonProperty("amount") BigDecimal amount,
    @JsonProperty("currency") String currency,
    @JsonProperty("status") String status,
    @JsonProperty("transactionId") String transactionId,
    @JsonProperty("failureReason") String failureReason,
    @JsonProperty("eventType") String eventType
) {}
