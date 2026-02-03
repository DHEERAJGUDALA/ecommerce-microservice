package com.ecommerce.payment.infrastructure.messaging;

import com.ecommerce.payment.application.dto.OrderEventPayload;
import com.ecommerce.payment.application.service.PaymentApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final PaymentApplicationService paymentApplicationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-events", groupId = "payment-service")
    public void consume(String message) {
        log.info("Received order event: {}", message);
        
        try {
            OrderEventPayload event = objectMapper.readValue(message, OrderEventPayload.class);
            
            if ("ORDER_CREATED".equals(event.eventType())) {
                paymentApplicationService.processOrderEvent(event);
            } else {
                log.debug("Ignoring event type: {}", event.eventType());
            }
        } catch (Exception e) {
            log.error("Failed to process order event: {}", message, e);
            // In production, you might want to send to DLQ or retry mechanism
        }
    }
}
