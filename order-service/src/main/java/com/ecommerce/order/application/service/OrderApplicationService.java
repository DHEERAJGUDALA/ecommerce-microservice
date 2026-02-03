package com.ecommerce.order.application.service;

import com.ecommerce.order.application.dto.CreateOrderRequest;
import com.ecommerce.order.application.dto.OrderDto;
import com.ecommerce.order.application.dto.OrderEventPayload;
import com.ecommerce.order.application.mapper.OrderApplicationMapper;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.entity.OrderItem;
import com.ecommerce.order.domain.entity.OutboxEvent;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.order.domain.repository.OutboxEventRepository;
import com.ecommerce.order.domain.valueobject.Address;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final OrderApplicationMapper mapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.customerId());

        Address shippingAddress = mapper.toDomain(request.shippingAddress());
        Address billingAddress = request.billingAddress() != null
            ? mapper.toDomain(request.billingAddress())
            : shippingAddress;

        Order order = Order.create(request.customerId(), shippingAddress, billingAddress);

        // TODO: In real implementation, fetch product details from product-service
        request.items().forEach(itemRequest -> {
            OrderItem item = OrderItem.create(
                itemRequest.productId(),
                "Product Name", // Should be fetched from product service
                "SKU-" + itemRequest.productId(),
                itemRequest.quantity(),
                new BigDecimal("99.99") // Should be fetched from product service
            );
            order.addItem(item);
        });

        order.setShippingCost(new BigDecimal("10.00"));
        order.confirm();

        Order savedOrder = orderRepository.save(order);

        OutboxEvent outboxEvent = createOutboxEvent(savedOrder, "ORDER_CREATED");
        outboxEventRepository.save(outboxEvent);

        log.info("Order created successfully: {} with outbox event: {}", savedOrder.getId(), outboxEvent.getId());

        return mapper.toDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(UUID orderId) {
        // TODO: Implement domain reconstruction from JPA
        throw new UnsupportedOperationException("Get order not yet implemented");
    }

    @Transactional
    public void markOrderAsPaid(UUID orderId) {
        log.info("Marking order as paid: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        order.markAsPaid();
        orderRepository.save(order);

        log.info("Order marked as paid successfully: {}", orderId);
    }

    @Transactional
    public void cancelOrder(UUID orderId, String reason) {
        log.info("Cancelling order: {} due to: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        order.cancel();
        orderRepository.save(order);

        log.info("Order cancelled successfully: {}", orderId);
    }

    @Transactional
    public void cancelOrder(UUID orderId, UUID customerId) {
        cancelOrder(orderId, "Cancelled by customer");
    }

    private OutboxEvent createOutboxEvent(Order order, String eventType) {
        try {
            OrderEventPayload payload = mapper.toEventPayload(order);
            String payloadJson = objectMapper.writeValueAsString(payload);

            return OutboxEvent.create(
                order.getId(),
                "Order",
                eventType,
                payloadJson
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize order event payload", e);
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }
}
