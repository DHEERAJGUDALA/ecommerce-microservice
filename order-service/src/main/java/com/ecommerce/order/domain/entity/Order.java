package com.ecommerce.order.domain.entity;

import com.ecommerce.order.domain.valueobject.Address;
import com.ecommerce.order.domain.valueobject.OrderStatus;
import com.ecommerce.common.exception.BusinessException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    private UUID id;
    private UUID customerId;
    private List<OrderItem> items;
    private Address shippingAddress;
    private Address billingAddress;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal total;
    private String currency;
    private Instant createdAt;
    private Instant updatedAt;

    private Order(UUID customerId, Address shippingAddress, Address billingAddress) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
        this.status = OrderStatus.PENDING;
        this.subtotal = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.shippingCost = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.currency = "USD";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Order create(UUID customerId, Address shippingAddress, Address billingAddress) {
        return new Order(customerId, shippingAddress, billingAddress);
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
        recalculateTotals();
    }

    public void removeItem(UUID productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
        recalculateTotals();
    }

    private void recalculateTotals() {
        this.subtotal = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.tax = subtotal.multiply(new BigDecimal("0.10")); // 10% tax
        this.total = subtotal.add(tax).add(shippingCost);
        this.updatedAt = Instant.now();
    }

    public void setShippingCost(BigDecimal cost) {
        this.shippingCost = cost;
        recalculateTotals();
    }

    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new BusinessException("INVALID_STATUS", "Order can only be confirmed from PENDING status");
        }
        if (items.isEmpty()) {
            throw new BusinessException("EMPTY_ORDER", "Cannot confirm order with no items");
        }
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    public void markAsPaid() {
        if (status != OrderStatus.CONFIRMED) {
            throw new BusinessException("INVALID_STATUS", "Order must be confirmed before marking as paid");
        }
        this.status = OrderStatus.PAID;
        this.updatedAt = Instant.now();
    }

    public void ship() {
        if (status != OrderStatus.PAID) {
            throw new BusinessException("INVALID_STATUS", "Order must be paid before shipping");
        }
        this.status = OrderStatus.SHIPPED;
        this.updatedAt = Instant.now();
    }

    public void deliver() {
        if (status != OrderStatus.SHIPPED) {
            throw new BusinessException("INVALID_STATUS", "Order must be shipped before delivery");
        }
        this.status = OrderStatus.DELIVERED;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new BusinessException("INVALID_STATUS", "Cannot cancel order that has been shipped or delivered");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
}
