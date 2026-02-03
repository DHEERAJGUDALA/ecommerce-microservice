package com.ecommerce.payment.interfaces.rest;

import com.ecommerce.payment.application.dto.CreatePaymentRequest;
import com.ecommerce.payment.application.dto.PaymentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Payment processing endpoints")
public class PaymentController {

    @PostMapping
    @Operation(summary = "Create a new payment")
    public ResponseEntity<PaymentDto> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        // TODO: Implement with PaymentService
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<PaymentDto> getPayment(@PathVariable UUID id) {
        // TODO: Implement with PaymentService
        return ResponseEntity.ok().build();
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID")
    public ResponseEntity<PaymentDto> getPaymentByOrder(@PathVariable UUID orderId) {
        // TODO: Implement with PaymentService
        return ResponseEntity.ok().build();
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get payments by customer")
    public ResponseEntity<List<PaymentDto>> getCustomerPayments(@PathVariable UUID customerId) {
        // TODO: Implement with PaymentService
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund a payment")
    public ResponseEntity<PaymentDto> refundPayment(@PathVariable UUID id) {
        // TODO: Implement with PaymentService
        return ResponseEntity.ok().build();
    }
}
