package com.ecommerce.payment.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderEventPayload(
    @JsonProperty("orderId") UUID orderId,
    @JsonProperty("customerId") UUID customerId,
    @JsonProperty("total") BigDecimal total,
    @JsonProperty("currency") String currency,
    @JsonProperty("status") String status,
    @JsonProperty("eventType") String eventType
) {}
