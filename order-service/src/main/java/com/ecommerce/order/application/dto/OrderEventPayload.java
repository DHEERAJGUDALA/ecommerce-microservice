package com.ecommerce.order.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderEventPayload(
    UUID orderId,
    UUID customerId,
    String status,
    BigDecimal total,
    List<OrderItemDto> items,
    Instant timestamp
) {}
