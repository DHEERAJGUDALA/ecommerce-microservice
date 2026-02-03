package com.ecommerce.payment.application.service;

import com.ecommerce.payment.application.dto.OrderEventPayload;
import com.ecommerce.payment.application.dto.PaymentEventPayload;
import com.ecommerce.payment.application.mapper.PaymentApplicationMapper;
import com.ecommerce.payment.domain.entity.OutboxEvent;
import com.ecommerce.payment.domain.entity.Payment;
import com.ecommerce.payment.domain.entity.ProcessedEvent;
import com.ecommerce.payment.domain.repository.OutboxEventRepository;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.payment.domain.repository.ProcessedEventRepository;
import com.ecommerce.payment.domain.valueobject.PaymentMethod;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApplicationService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final PaymentApplicationMapper mapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void processOrderEvent(OrderEventPayload event) {
        log.info("Processing order event: {}", event.orderId());

        // 1. Check if already processed (idempotency)
        if (processedEventRepository.existsById(event.orderId())) {
            log.info("Order {} already processed, skipping", event.orderId());
            return;
        }

        // 2. Create Payment with status based on amount (simulation)
        Payment payment = Payment.create(
            event.orderId(),
            event.customerId(),
            event.total(),
            event.currency(),
            PaymentMethod.CREDIT_CARD
        );

        // 3. Simulate payment processing
        if (event.total().compareTo(new BigDecimal("10000")) > 0) {
            // Fail payments over 10000
            payment.fail("INSUFFICIENT_FUNDS");
            log.warn("Payment failed for order {}: amount {} exceeds limit", event.orderId(), event.total());
        } else {
            // Success for amounts <= 10000
            payment.process("TXN-" + UUID.randomUUID());
            payment.complete();
            log.info("Payment completed successfully for order {}", event.orderId());
        }

        // 4. Save Payment
        paymentRepository.save(payment);

        // 5. Create OutboxEvent (PAYMENT_COMPLETED or PAYMENT_FAILED)
        String eventType = payment.isSuccessful() ? "PAYMENT_COMPLETED" : "PAYMENT_FAILED";
        OutboxEvent outboxEvent = createOutboxEvent(payment, eventType);
        outboxEventRepository.save(outboxEvent);

        // 6. Record processed event (idempotency marker)
        ProcessedEvent processedEvent = new ProcessedEvent(event.orderId(), "ORDER_CREATED");
        processedEventRepository.save(processedEvent);

        log.info("Order event processed successfully: {} -> {}", event.orderId(), eventType);
    }

    private OutboxEvent createOutboxEvent(Payment payment, String eventType) {
        PaymentEventPayload payload = mapper.toPaymentEventPayload(payment);
        
        // Create new payload with eventType set
        PaymentEventPayload completePayload = new PaymentEventPayload(
            payload.paymentId(),
            payload.orderId(),
            payload.customerId(),
            payload.amount(),
            payload.currency(),
            payload.status(),
            payload.transactionId(),
            payload.failureReason(),
            eventType
        );

        try {
            String payloadJson = objectMapper.writeValueAsString(completePayload);
            return OutboxEvent.create(
                payment.getId(),
                "Payment",
                eventType,
                payloadJson
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payment event payload", e);
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }
}
