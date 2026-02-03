package com.ecommerce.payment.application.dto;

import com.ecommerce.payment.domain.valueobject.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(
        @NotNull(message = "Order ID is required")
        UUID orderId,

        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        String currency,

        @NotNull(message = "Payment method is required")
        PaymentMethod method
) {
    public CreatePaymentRequest {
        if (currency == null || currency.isBlank()) {
            currency = "USD";
        }
    }
}
