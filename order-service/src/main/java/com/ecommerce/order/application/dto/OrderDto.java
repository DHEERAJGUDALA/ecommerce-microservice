package com.ecommerce.order.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderDto(
        UUID id,
        UUID customerId,
        List<OrderItemDto> items,
        AddressDto shippingAddress,
        AddressDto billingAddress,
        String status,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal shippingCost,
        BigDecimal total,
        String currency,
        Instant createdAt,
        Instant updatedAt
) {}
