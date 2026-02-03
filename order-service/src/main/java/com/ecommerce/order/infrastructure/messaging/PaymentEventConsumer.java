package com.ecommerce.order.infrastructure.messaging;

import com.ecommerce.order.application.dto.PaymentEventPayload;
import com.ecommerce.order.application.service.OrderApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderApplicationService orderApplicationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-events", groupId = "order-service")
    public void consume(String message) {
        log.info("Received payment event: {}", message);
        
        try {
            PaymentEventPayload event = objectMapper.readValue(message, PaymentEventPayload.class);
            
            if ("PAYMENT_COMPLETED".equals(event.eventType())) {
                orderApplicationService.markOrderAsPaid(event.orderId());
            } else if ("PAYMENT_FAILED".equals(event.eventType())) {
                orderApplicationService.cancelOrder(event.orderId(), event.failureReason());
            } else {
                log.debug("Ignoring event type: {}", event.eventType());
            }
        } catch (Exception e) {
            log.error("Failed to process payment event: {}", message, e);
            // In production, you might want to send to DLQ or retry mechanism
        }
    }
}
